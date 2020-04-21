package com.enjin.enjincraft.spigot.token;

import lombok.ToString;

import java.util.*;

@ToString
public class TokenPermissionGraph {

    private Map<String, Map<String, Set<String>>> permissionTokens = new HashMap<>(); // World -> (Permission -> Tokens)
    private Map<String, Map<String, Set<String>>> tokenPermissions = new HashMap<>(); // Token -> (World -> Permissions)

    protected void addToken(Map<String, Set<String>> permsMap, String tokenId) {
        permsMap.forEach(((world, perms) -> {
            perms.forEach(perm -> addTokenPerm(perm, tokenId, world));
        }));
    }

    protected void addToken(TokenModel tokenModel) {
        addToken(tokenModel.getPermissionsMap(), tokenModel.getId());
    }

    protected void addTokenPerm(String perm, String tokenId, String world) {
        Map<String, Set<String>> worldTokens = getPermissionTokens(world);  // Permission -> Tokens
        Map<String, Set<String>> worldPerms = getTokenPermissions(tokenId); // World -> Permissions

        if (worldTokens == null) {
            worldTokens = new HashMap<>();
            worldTokens.put(perm, new HashSet<>());
            permissionTokens.put(world, worldTokens);
        }

        if (worldPerms == null) {
            worldPerms = new HashMap<>();
            worldPerms.put(world, new HashSet<>());
            tokenPermissions.put(tokenId, worldPerms);
        }

        Set<String> tokens = worldTokens.get(perm);
        Set<String> perms = worldPerms.get(world);

        if (tokens == null) {
            tokens = new HashSet<>();
            worldTokens.put(perm, tokens);
        }

        if (perms == null) {
            perms = new HashSet<>();
            worldPerms.put(world, perms);
        }

        tokens.add(tokenId);
        perms.add(perm);

        // Removes permission from all other worlds if world was set to global
        if (world.equals(TokenManager.GLOBAL)) {
            Set<String> nonGlobals = new HashSet<>(worldPerms.keySet());
            nonGlobals.remove(TokenManager.GLOBAL);
            removeTokenPerm(perm, tokenId, nonGlobals);
        } else {
            removeTokenPermGlobal(perm, tokenId);
        }
    }

    protected void addTokenPerm(String perm, String tokenId, Collection<String> worlds) {
        worlds.forEach(world -> addTokenPerm(perm, tokenId, world));
    }

    protected void removeToken(Map<String, Set<String>> permsMap, String tokenId) {
        permsMap.forEach((world, perms) -> {
            perms.forEach(perm -> removeTokenPerm(perm, tokenId, world));
        });
    }

    protected void removeToken(TokenModel tokenModel) {
        removeToken(tokenModel.getPermissionsMap(), tokenModel.getId());
    }

    protected void removeTokenPerm(String perm, String tokenId, String world) {
        Map<String, Set<String>> worldTokens = getPermissionTokens(world);  // Permission -> Tokens
        Map<String, Set<String>> worldPerms = getTokenPermissions(tokenId); // World -> Permissions
        Set<String> tokens = null;
        Set<String> perms = null;

        if (worldTokens != null)
            tokens = worldTokens.get(perm);
        if (worldPerms != null)
            perms = worldPerms.get(world);

        if (tokens != null)
            tokens.remove(tokenId);
        if (perms != null)
            perms.remove(perm);

        // Removes permission from all other worlds if world was set to global
        if (worldPerms != null && world.equals(TokenManager.GLOBAL)) {
            Set<String> nonGlobals = new HashSet<>(worldPerms.keySet());
            nonGlobals.remove(TokenManager.GLOBAL);
            removeTokenPerm(perm, tokenId, nonGlobals);
        }
    }

    protected void removeTokenPerm(String perm, String tokenId, Collection<String> worlds) {
        worlds.forEach(world -> removeTokenPerm(perm, tokenId, world));
    }

    private void removeTokenPermGlobal(String perm, String tokenId) {
        Map<String, Set<String>> worldTokens = getPermissionTokens(TokenManager.GLOBAL); // Permission -> Tokens
        Map<String, Set<String>> worldPerms = getTokenPermissions(tokenId);              // World -> Permissions
        Set<String> tokens = null;
        Set<String> perms = null;

        if (worldTokens != null)
            tokens = worldTokens.get(perm);
        if (worldPerms != null)
            perms = worldPerms.get(TokenManager.GLOBAL);

        if (tokens != null)
            tokens.remove(tokenId);
        if (perms != null)
            perms.remove(perm);
    }

    public Map<String, Set<String>> getPermissionTokens(String world) {
        return permissionTokens.get(world);
    }

    public Map<String, Set<String>> getTokenPermissions(String tokenId) {
        return tokenPermissions.get(tokenId);
    }

}
