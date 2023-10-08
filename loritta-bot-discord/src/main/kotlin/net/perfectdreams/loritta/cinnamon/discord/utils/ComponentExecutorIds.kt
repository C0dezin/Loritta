package net.perfectdreams.loritta.cinnamon.discord.utils

import net.perfectdreams.loritta.cinnamon.discord.interactions.ComponentId

object ComponentExecutorIds {
    private val ID_REGEX = Regex("[A-z0-9]+")
    private val registeredComponents = mutableSetOf<ComponentId>()

    // All component executors can have any character from A-z0-9
    // This means that we can have 1185921 executors, which should be enough :)
    val CHANGE_CATEGORY_MENU_EXECUTOR = register("0000")
    val PORTRAIT_SELECT_MENU_EXECUTOR = register("0001")
    val CHANGE_TOBY_CHARACTER_MENU_EXECUTOR = register("0002")
    val CHANGE_DIALOG_BOX_TYPE_BUTTON_EXECUTOR = register("0003")
    val CHANGE_UNIVERSE_SELECT_MENU_EXECUTOR = register("0004")
    val CONFIRM_DIALOG_BOX_BUTTON_EXECUTOR = register("0005")
    val CHANGE_COLOR_PORTRAIT_TYPE_BUTTON_EXECUTOR = register("0006")
    val CHANGE_TRANSACTION_PAGE_BUTTON_EXECUTOR = register("0007")
    val CHANGE_TRANSACTION_FILTER_SELECT_MENU_EXECUTOR = register("0008")
    val START_MATCHMAKING_BUTTON_EXECUTOR = register("0009")
    val SWITCH_TO_GUILD_PROFILE_AVATAR_EXECUTOR = register("0010")
    val SWITCH_TO_GLOBAL_AVATAR_EXECUTOR = register("0011")
    val FOLLOW_PACKAGE_BUTTON_EXECUTOR = register("0012")
    val UNFOLLOW_PACKAGE_BUTTON_EXECUTOR = register("0013")
    val SELECT_PACKAGE_SELECT_MENU_EXECUTOR = register("0014")
    val GO_BACK_TO_PACKAGE_LIST_BUTTON_EXECUTOR = register("0015")
    val TRACK_PACKAGE_BUTTON_EXECUTOR = register("0016")
    val RETRIBUTE_HUG_BUTTON_EXECUTOR = register("0017")
    val RETRIBUTE_HEAD_PAT_BUTTON_EXECUTOR = register("0018")
    val RETRIBUTE_HIGH_FIVE_BUTTON_EXECUTOR = register("0019")
    val SOURCE_PICTURE_EXECUTOR = register("0020")
    val RETRIBUTE_SLAP_BUTTON_EXECUTOR = register("0021")
    val RETRIBUTE_ATTACK_BUTTON_EXECUTOR = register("0022")
    val RETRIBUTE_DANCE_BUTTON_EXECUTOR = register("0023")
    val RETRIBUTE_KISS_BUTTON_EXECUTOR = register("0024")
    val SHOW_GUILD_MEMBER_PERMISSIONS_BUTTON_EXECUTOR = register("0025")
    val CHANGE_NOTIFICATIONS_PAGE_BUTTON_EXECUTOR = register("0026")
    val ACTIVATE_INVITE_BLOCKER_BYPASS_BUTTON_EXECUTOR = register("0027")
    val TRACK_PACKAGE_MODAL_EXECUTOR = register("0028")
    val PLAY_AUDIO_CLIP_BUTTON_EXECUTOR = register("0029")
    val TRANSFER_SONHOS_BUTTON_EXECUTOR = register("0030")
    val CANCEL_SONHOS_TRANSFER_BUTTON_EXECUTOR = register("0031")
    val CONFIRM_BAN_BUTTON_EXECUTOR = register("0032")
    val CHANGE_ABOUT_ME_BUTTON_EXECUTOR = register("0033")
    val CHANGE_ABOUT_ME_MODAL_SUBMIT_EXECUTOR = register("0034")
    val CHANGE_XP_RANK_PAGE_BUTTON_EXECUTOR = register("0035")
    val CHANGE_SONHOS_RANK_PAGE_BUTTON_EXECUTOR = register("0036")
    val CHANGE_TICKER_CATEGORY_MENU_EXECUTOR = register("0037")
    val OCR_TRANSLATE_BUTTON_EXECUTOR = register("0038")
    val ACCEPT_COIN_FLIP_BET_FRIEND_EXECUTOR = register("0039")
    val REMATCH_COIN_FLIP_BET_FRIEND_EXECUTOR = register("0040")

    /**
     * Verifies if the [id] matches our constraints
     *
     * * The ID must match the [ID_REGEX]
     * * The ID must have four characters
     *
     * @return the id
     */
    fun register(id: String): ComponentId {
        require(ID_REGEX.matches(id)) { "ID must respect the $ID_REGEX regular expression!" }
        require(id.length == 4) { "ID must have four characters!" }
        val componentId = ComponentId(id)
        require(componentId !in registeredComponents) { "There is already an component with ID $id!" }
        registeredComponents.add(componentId)
        return componentId
    }
}