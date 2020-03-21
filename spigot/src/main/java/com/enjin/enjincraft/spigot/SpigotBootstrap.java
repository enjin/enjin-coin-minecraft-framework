package com.enjin.enjincraft.spigot;

import com.enjin.enjincraft.spigot.cmd.CmdEnj;
import com.enjin.enjincraft.spigot.configuration.Conf;
import com.enjin.enjincraft.spigot.configuration.TokenConf;
import com.enjin.enjincraft.spigot.hooks.PlaceholderApiExpansion;
import com.enjin.enjincraft.spigot.i18n.Translation;
import com.enjin.enjincraft.spigot.listeners.EnjEventListener;
import com.enjin.enjincraft.spigot.listeners.TokenItemListener;
import com.enjin.enjincraft.spigot.player.PlayerManager;
import com.enjin.enjincraft.spigot.storage.Database;
import com.enjin.enjincraft.spigot.trade.TradeManager;
import com.enjin.enjincraft.spigot.trade.TradeUpdateTask;
import com.enjin.enjincraft.spigot.util.MessageUtils;
import com.enjin.sdk.TrustedPlatformClient;
import com.enjin.sdk.TrustedPlatformClientBuilder;
import com.enjin.sdk.graphql.GraphQLResponse;
import com.enjin.sdk.http.HttpResponse;
import com.enjin.sdk.model.service.auth.AuthTokens;
import com.enjin.sdk.model.service.platform.PlatformDetails;
import com.enjin.sdk.service.notifications.NotificationsService;
import com.enjin.sdk.service.notifications.PusherNotificationService;
import com.enjin.java_commons.StringUtils;
import io.sentry.Sentry;
import io.sentry.jul.SentryHandler;
import net.kyori.text.TextComponent;
import net.kyori.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;

import java.io.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Handler;
import java.util.logging.Logger;

import static okhttp3.logging.HttpLoggingInterceptor.Level.BODY;
import static okhttp3.logging.HttpLoggingInterceptor.Level.NONE;

public class SpigotBootstrap implements Bootstrap, Module {

    private final EnjPlugin plugin;
    private Conf conf;
    private TokenConf tokenConf;
    private Database database;
    private Handler sentryHandler;

    private TrustedPlatformClient trustedPlatformClient;
    private PlatformDetails platformDetails;
    private NotificationsService notificationsService;
    private PlayerManager playerManager;
    private TradeManager tradeManager;

    public SpigotBootstrap(EnjPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void setUp() {
        try {
            if (!initConfig())
                return;

            tokenConf = new TokenConf(plugin);
            tokenConf.load();

            if (!StringUtils.isEmpty(conf.getSentryUrl())) {
                sentryHandler = new SentryHandler();
                Sentry.init(String.format("%s?release=%s&stacktrace.app.packages=com.enjin",
                        conf.getSentryUrl(),
                        plugin.getDescription().getVersion()));
                getLogger().addHandler(sentryHandler);
            }

            loadLocales();

            database = new Database(this);

            // Create the trusted platform client
            trustedPlatformClient = new TrustedPlatformClientBuilder()
                    .httpLogLevel(conf.isSdkDebugEnabled() ? BODY : NONE)
                    .baseUrl(conf.getBaseUrl())
                    .readTimeout(1, TimeUnit.MINUTES)
                    .build();

            authenticateTPClient();
            fetchPlatformDetails();
            startNotificationService();

            // Init Managers
            playerManager = new PlayerManager(this);
            tradeManager = new TradeManager(this);

            // Register Listeners
            Bukkit.getPluginManager().registerEvents(playerManager, plugin);
            Bukkit.getPluginManager().registerEvents(tradeManager, plugin);
            Bukkit.getPluginManager().registerEvents(new TokenItemListener(this), plugin);

            // Register Commands
            PluginCommand pluginCommand = plugin.getCommand("enj");
            CmdEnj cmdEnj = new CmdEnj(this);
            pluginCommand.setExecutor(cmdEnj);

            if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
                MessageUtils.sendComponent(Bukkit.getConsoleSender(), TextComponent.of("[EnjinCraft] Registering PlaceholderAPI Expansion")
                        .color(TextColor.GOLD));
                boolean registered = new PlaceholderApiExpansion(this).register();
                if (registered) {
                    MessageUtils.sendComponent(Bukkit.getConsoleSender(), TextComponent.of("[EnjinCraft] Registered PlaceholderAPI Expansion")
                            .color(TextColor.GREEN));
                } else {
                    MessageUtils.sendComponent(Bukkit.getConsoleSender(), TextComponent.of("[EnjinCraft] Could not register PlaceholderAPI Expansion")
                            .color(TextColor.RED));
                }
            }

            new TradeUpdateTask(this).runTaskTimerAsynchronously(plugin, 20, 20);
        } catch (Exception ex) {
            log(ex);
            Bukkit.getPluginManager().disablePlugin(plugin);
        }
    }

    private boolean initConfig() {
        plugin.saveDefaultConfig();

        conf = new Conf(plugin.getConfig());

        // Validate that the required config values are valid
        if (!validateConfig()) {
            Bukkit.getPluginManager().disablePlugin(plugin);
            return false;
        }

        return true;
    }

    private void authenticateTPClient() {
        HttpResponse<GraphQLResponse<AuthTokens>> networkResponse;

        try {
            // Attempt to authenticate the client using an app secret
            networkResponse = trustedPlatformClient.authAppSync(
                    conf.getAppId(),
                    conf.getAppSecret()
            );
        } catch (Exception ex) {
            throw new AuthenticationException(ex);
        }

        // Could not authenticate the client
        if (!networkResponse.isSuccess()) {
            throw new AuthenticationException(networkResponse.code());
        }
    }

    private void fetchPlatformDetails() {
        try {
            // Fetch the platform details
            HttpResponse<GraphQLResponse<PlatformDetails>> networkResponse = trustedPlatformClient.getPlatformService()
                    .getPlatformSync();

            if (!networkResponse.isSuccess())
                throw new NetworkException(networkResponse.code());

            GraphQLResponse<PlatformDetails> graphQLResponse = networkResponse.body();

            if (!graphQLResponse.isSuccess())
                throw new GraphQLException(graphQLResponse.getErrors());

            platformDetails = graphQLResponse.getData();
        } catch (Exception ex) {
            throw new NetworkException(ex);
        }
    }

    private void startNotificationService() {
        try {
            // Start the notification service and register a listener
            notificationsService = new PusherNotificationService(platformDetails);
            notificationsService.start();
            notificationsService.registerListener(new EnjEventListener(this));
            notificationsService.subscribeToApp(conf.getAppId());
        } catch (Exception ex) {
            throw new NotificationServiceException(ex);
        }
    }

    @Override
    public void tearDown() {
        try {
            if (trustedPlatformClient != null)
                trustedPlatformClient.close();
            if (notificationsService != null)
                notificationsService.shutdown();
        } catch (Exception ex) {
            log(ex);
        }

        if (sentryHandler != null)
            getLogger().removeHandler(sentryHandler);
    }

    @Override
    public TrustedPlatformClient getTrustedPlatformClient() {
        return trustedPlatformClient;
    }

    @Override
    public NotificationsService getNotificationsService() {
        return notificationsService;
    }

    @Override
    public PlayerManager getPlayerManager() {
        return playerManager;
    }

    public TradeManager getTradeManager() {
        return tradeManager;
    }

    @Override
    public Conf getConfig() {
        return conf;
    }

    @Override
    public TokenConf getTokenConf() {
        return tokenConf;
    }

    public Plugin plugin() {
        return plugin;
    }

    private boolean validateConfig() {
        boolean validUrl = !StringUtils.isEmpty(conf.getBaseUrl());
        boolean validAppId = conf.getAppId() >= 0;
        boolean validSecret = !StringUtils.isEmpty(conf.getAppSecret());
        boolean validIdentityId = conf.getDevIdentityId() >= 0;

        if (!validUrl)
            plugin.getLogger().warning("Invalid platform url specified in config.");
        if (!validAppId)
            plugin.getLogger().warning("Invalid app id specified in config.");
        if (!validSecret)
            plugin.getLogger().warning("Invalid app secret specified in config.");
        if (!validIdentityId)
            plugin.getLogger().warning("Invalid dev identity id specified in config.");

        return validUrl && validAppId && validSecret && validIdentityId;
    }

    public void loadLocales() {
        Translation.setServerLocale(conf.getLocale());
        Translation.loadLocales(plugin);
    }

    public void debug(String log) {
        if (conf.isPluginDebugEnabled())
            getLogger().info(log);
    }

    public Logger getLogger() {
        return plugin.getLogger();
    }

    public void log(Throwable throwable) {
        plugin.log(throwable);
    }

    public Database db() {
        return database;
    }
}
