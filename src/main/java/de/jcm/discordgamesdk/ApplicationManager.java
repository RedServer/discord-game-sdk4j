package de.jcm.discordgamesdk;

import de.jcm.discordgamesdk.impl.Command;
import de.jcm.discordgamesdk.impl.commands.Authenticate;
import de.jcm.discordgamesdk.user.DiscordUser;
import lombok.Value;
import lombok.experimental.Accessors;

import java.util.Date;
import java.util.Set;
import java.util.function.BiConsumer;

public class ApplicationManager
{
    /**
     * A Discord OAuth2 token.
     */
    @Value
    @Accessors(fluent = true)
    public static class DiscordOAuth2Token {

        /** The access token itself. */
        String accessToken;
        /** A set of the scopes the token is authorized for. */
        Set<String> scopes;
        /** Expiration date of the token. */
        Date expires;
    }

    /**
     * A Discord application.
     */
    @Value
    @Accessors(fluent = true)
    public static class Application {

        /** ID of the application. */
        long id;
        /** Name of the application. */
        String name;
        /** Asset ID of the icon of the application. */
        String icon;
        /** Description of the application. */
        String description;
        /** Type of the application. */
        String type;
        /** Asset ID of the cover image of the application. */
        String coverImage;
        /** Summary of the application. */
        String summary;
        /** {@code true} if the application is monetized. */
        boolean monetized;
        /** {@code true} if the application is verified. */
        boolean verified;
        /** Verification key of the application. */
        String verifyKey;
        /** Flags of the application. */
        int flags;
        /** Whether the application uses a hook. */
        boolean hook;
        /** {@code true} if the application is available on the storefront. */
        boolean storefrontAvailable;
    }

    /**
     * Combined data consisting of a {@link DiscordOAuth2Token}, an {@link Application}, and a {@link DiscordUser}
     */
    @Value
    @Accessors(fluent = true)
    public static class AuthenticationData {

        /** Obtained token. */
        DiscordOAuth2Token token;
        /** Information about this application. */
        Application application;
        /** Current Discord user. */
        DiscordUser user;
    }

    private final Core.CorePrivate core;

    ApplicationManager(Core.CorePrivate core)
    {
        this.core = core;
    }

    /**
     * Requests authorization from the user (if not obtained already) and returns
     * the OAuth2 token in the callback.
     * @param callback Callback to process the returned {@link Result} and {@link DiscordOAuth2Token}.
     */
    public void getOAuth2Token(BiConsumer<Result, DiscordOAuth2Token> callback) {
        core.sendCommand(Command.Type.AUTHENTICATE, new Object(), c->{
            Result r = core.checkError(c);
            if(r != Result.OK)
            {
                callback.accept(r, null);
                return;
            }
            Authenticate.Response response = core.getGson().fromJson(c.getData(), Authenticate.Response.class);
            callback.accept(r, response.toDiscordOAuth2Token());
        });
    }

    /**
     * Requests authorization from the user (if not obtained already) and returns
     * the OAuth2 token and some additional information about user and application in a callback.
     * @param callback Callback to process the returned {@link Result} and {@link AuthenticationData}.
     */
    public void authenticate(BiConsumer<Result, AuthenticationData> callback) {
        core.sendCommand(Command.Type.AUTHENTICATE, new Object(), c->{
            Result r = core.checkError(c);
            if(r != Result.OK)
            {
                callback.accept(r, null);
                return;
            }
            Authenticate.Response response = core.getGson().fromJson(c.getData(), Authenticate.Response.class);
            DiscordOAuth2Token token = response.toDiscordOAuth2Token();
            DiscordUser user = response.user;
            Application application = response.application.toApplication();

            callback.accept(r, new AuthenticationData(token, application, user));
        });
    }
}
