package org.mvplugins.multiverse.portals.locale;

import java.util.Locale;

import org.mvplugins.multiverse.core.locale.message.Message;
import org.mvplugins.multiverse.core.locale.message.MessageReplacement;
import org.mvplugins.multiverse.external.acf.locales.MessageKey;
import org.mvplugins.multiverse.external.acf.locales.MessageKeyProvider;
import org.mvplugins.multiverse.external.jetbrains.annotations.ApiStatus;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;

/**
 * All translation keys for Multiverse-Portals.
 *
 * @since 5.3
 */
@ApiStatus.AvailableSince("5.3")
@ApiStatus.Internal
public enum MVPi18n implements MessageKeyProvider {
    // BEGIN CHECKSTYLE-SUPPRESSION: JavadocVariable

    // /mvp config
    CONFIG_DESCRIPTION,
    CONFIG_PROPERTY_DESCRIPTION,
    CONFIG_VALUE_DESCRIPTION,

    // /mvp create
    CREATE_DESCRIPTION,
    CREATE_NAME_DESCRIPTION,
    CREATE_DESTINATION_DESCRIPTION,
    CREATE_INVALIDNAME,
    CREATE_SUCCESS,
    CREATE_ALREADYEXISTS,
    CREATE_ACTIONNOTSET,

    // /mvp debug
    DEBUG_DESCRIPTION,
    DEBUG_TOGGLE_DESCRIPTION,
    DEBUG_ENABLED,
    DEBUG_DISABLED,

    // /mvp info
    INFO_DESCRIPTION,
    INFO_PORTAL_DESCRIPTION,

    // /mvp list
    LIST_DESCRIPTION,
    LIST_FILTER_DESCRIPTION,
    LIST_PAGE_DESCRIPTION,
    LIST_HEADER,
    LIST_HEADER_WORLD,
    LIST_HEADER_FILTER,
    LIST_ENTRY,

    // /mvp modify
    MODIFY_DESCRIPTION,
    MODIFY_PORTAL_DESCRIPTION,
    MODIFY_PROPERTY_DESCRIPTION,
    MODIFY_VALUE_DESCRIPTION,
    MODIFY_HEREDEPRECATED,
    MODIFY_SAVEFAILED,
    MODIFY_SUCCESS,
    MODIFY_ACTIONTYPEWARNING,
    MODIFY_FAILURE,

    // /mvp remove
    REMOVE_DESCRIPTION,
    REMOVE_NAME_DESCRIPTION,
    REMOVE_NOTFOUND,
    REMOVE_SUCCESS,

    // /mvp select
    SELECT_DESCRIPTION,
    SELECT_PORTAL_DESCRIPTION,
    SELECT_NONE,
    SELECT_CURRENT,
    SELECT_SUCCESS,

    // /mvp wand
    WAND_DESCRIPTION,
    WAND_ACTION_DESCRIPTION,
    WAND_INVALIDACTION,
    WAND_WORLDEDIT,
    WAND_GIVEN,
    WAND_INVENTORY,
    WAND_DROPPED,

    // Command contexts
    CONTEXT_PORTAL_PLAYERSELECTIONREQUIRED,
    CONTEXT_PORTAL_NOTFOUND,

    // Portal selection
    SELECTION_FIRST,
    SELECTION_FIRST_AREA,
    SELECTION_SECOND,
    SELECTION_SECOND_AREA,
    SELECTION_WORLDEDIT_INCOMPLETE,
    SELECTION_WORLDEDIT_REQUIRED,
    SELECTION_LEFT_REQUIRED,
    SELECTION_RIGHT_REQUIRED,
    SELECTION_SAMEWORLD_REQUIRED,

    // Portal information
    PORTALINFO_HEADER,
    PORTALINFO_COMMANDHEADER,
    PORTALINFO_DEBUGHEADER,
    PORTALINFO_CUSTOMHEADER,
    PORTALINFO_COORDINATES,
    PORTALINFO_WORLD_ERROR,
    PORTALINFO_ACTIONTYPE,
    PORTALINFO_ACTION,
    PORTALINFO_ACTION_NOTSET,
    PORTALINFO_CHECKDESTINATIONSAFETY,
    PORTALINFO_TELEPORTNONPLAYERS,
    PORTALINFO_PRICE,
    PORTALINFO_PRICE_FREE,
    PORTALINFO_PLAYERACTION,
    PORTALINFO_ACTIONERROR,

    // Portal destinations
    DESTINATION_WORLD,
    DESTINATION_LOCATION,
    DESTINATION_RAW,
    DESTINATION_INVALIDLOCATION,
    DESTINATION_INVALID,
    DESTINATION_PORTAL_INVALIDFORMAT,
    DESTINATION_PORTAL_NOTFOUND,

    // Portal actions
    ACTION_UNKNOWN_TYPE,
    ACTION_COMMAND_INVALID,
    ACTION_COMMAND_SELF,
    ACTION_COMMAND_OPERATOR,
    ACTION_COMMAND_CONSOLE,
    ACTION_MULTIVERSEDESTINATION_INVALID,
    ACTION_MULTIVERSEDESTINATION_DESCRIPTION,
    ACTION_SERVER_INVALID,
    ACTION_SERVER_PLAYERSONLY,
    ACTION_SERVER_PROXYERROR,
    ACTION_SERVER_PROXYUNKNOWNERROR,
    ACTION_SERVER_DESCRIPTION,

    // Portal use
    PORTAL_ACTION_SUCCESS,
    PORTAL_PERMISSION_DENIED,
    PORTAL_FRAME_INVALID,
    PORTAL_DESTINATION_INVALID,
    PORTAL_INSUFFICIENTFUNDS,
    PORTAL_COOLDOWN,
    PORTAL_FILL_INVALIDSIZE,

    // Portal configuration
    PORTALCONFIG_LOCATION_PLAYERSONLY,
    PORTALCONFIG_LOCATION_SELECTIONREQUIRED,
    PORTALCONFIG_LOCATION_INVALID,

    // Generic
    GENERIC_TRUE,
    GENERIC_FALSE,
    GENERIC_ERROR_DETAILS;

    // END CHECKSTYLE-SUPPRESSION: JavadocVariable

    private final MessageKey key = MessageKey.of("mv-portals." + this.name().replace('_', '.')
            .toLowerCase(Locale.ENGLISH));

    /**
     * {@inheritDoc}
     *
     * @since 5.3
     */
    @Override
    @ApiStatus.AvailableSince("5.3")
    public MessageKey getMessageKey() {
        return this.key;
    }

    /**
     * Creates a message with a non-localized fallback and replacements.
     *
     * @param nonLocalizedMessage The non-localized fallback message.
     * @param replacements The replacements.
     * @return A new localizable message.
     *
     * @since 5.3
     */
    @ApiStatus.AvailableSince("5.3")
    @NotNull public Message bundle(
            @NotNull String nonLocalizedMessage,
            @NotNull MessageReplacement... replacements) {
        return Message.of(this, nonLocalizedMessage, replacements);
    }
}
