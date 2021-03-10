// @flow
import { device, element, by, waitFor } from 'detox'

export const IOS = 'ios'
export const ANDROID = 'android'

// Don't access via static import, use getDeviceType
let DEVICE_TYPE = IOS
// let DEVICE_TYPE = ANDROID
export function setDeviceType(platform: string) {
  DEVICE_TYPE = platform
}
export const getDeviceType = () => DEVICE_TYPE

// common
export const OK_TEXT_ALERT = 'OK'
export const NATIVE_ALERT_TYPE = () =>
  DEVICE_TYPE === IOS ? '_UIAlertControllerActionView' : 'android.widget.Button'
export const NATIVE_ALERT_OK_MATCHER = () =>
  DEVICE_TYPE === IOS
    ? by.label(OK_TEXT_ALERT).and(by.type(NATIVE_ALERT_TYPE()))
    : by.text(OK_TEXT_ALERT).and(by.type(NATIVE_ALERT_TYPE()))
export const TEST_PASS_CODE = '000000'
export const TEST_PASS_CODE_CHANGED = '111111'
export const USER_AVATAR = 'user-avatar'
export const BACK_ARROW = 'back-arrow-touchable'
export const BURGER_MENU = 'burger-menu'
export const SCAN_BUTTON = 'Scan'
export const ALLOW_BUTTON = 'Allow Push Notifications'
export const CONNECT_BUTTON = 'Connect'
export const NEW_BACK_ARROW = 'left-icon'

// home
export const HOME_FEEDBACK_BUTTON = 'home-feedback-id-label'
export const HOME_CONTAINER = 'home-container'
export const HOME_HEADER = 'Home'
export const HOME_NEW_MESSAGE = 'NEW MESSAGE - TAP TO OPEN'

// claim offer
export const CLAIM_OFFER_ICON_CLOSE = 'claim-offer-icon-close-touchable'
// export const CLAIM_OFFER_ACCEPT = 'claim-offer-footer-accept'
// export const CLAIM_OFFER_DENY = 'claim-offer-footer-deny'
export const CLAIM_OFFER_ACCEPT = 'Accept Credential'
export const CLAIM_OFFER_REJECT = 'Reject'
export const CLAIM_OFFER_SUCCESS_MODAL_CONTINUE =
  'claim-request-success-continue'

// connection history
export const HISTORY_ICON_CLOSE = 'connection-history-icon-close-touchable'
export const HISTORY_ICON_DELETE = 'connection-history-icon-delete-touchable'
export const HISTORY_DETAIL_BACK = 'history-details-back-arrow-touchable'

// expired token
export const EXPIRED_TOKEN = 'expired-token-container'

//Request
export const REQUEST_CONTAINER = 'request-container'

// invitation
export const INVITATION_ACCEPT = 'invitation-accept'
export const INVITATION_DECLINE = 'invitation-deny'
export const INVITATION_SUCCESS_MODAL_CONTINUE = 'invitation-success-continue'
export const ACCEPT_INVITATION_LABEL = 'Allow'

// lock related screens
export const LOCK_SELECTION_OR_TEXT = 'lock-selection-or-text-touchable'
export const LOCK_SELECTION_VIEW = 'lock-selection-view'
export const LOCK_SELECTION_PIN_CODE = 'pin-code-selection-touchable'
export const LOCK_SELECTION_TOUCH_ID = 'touch-id-selection-touchable'
export const LOCK_SETUP_SUCCESS_CLOSE_BUTTON = 'close-button'
export const PIN_CODE_INPUT_BOX = 'pin-code-input-box'

// proof request
// export const PROOF_REQUEST_ACCEPT = 'proof-request-accept'
// export const PROOF_REQUEST_DENY = 'proof-request-deny'
export const PROOF_REQUEST_SEND = 'Share Attributes'
export const PROOF_REQUEST_REJECT = 'Reject'
export const PROOF_REQUEST_GENERATE = 'Generate'
export const PROOF_REQUEST_ICON_CLOSE = 'proof-request-icon-close-touchable'
export const PROOF_REQUEST_MODAL_CONTINUE = 'send-proof-success-continue'
export const GENERAL_SCROLL_VIEW = 'RCTCustomScrollView'
// export const GENERAL_SCROLL_VIEW = 'RCTScrollContentView'
export const PROOF_REQUEST_MISSING_ATTRIBUTE_BASE =
  'proof-request-attribute-item-input-missing attribute '

// qr-code
export const QR_CODE_INPUT_ENV_SWITCH = 'qr-code-text-input-env-switch'
export const QR_CODE_ENV_SWITCH_URL =
  'https://s3-us-west-2.amazonaws.com/vcx-env/dev'
export const QR_CODE_NATIVE_ALERT_SWITCH_TEXT = 'Switch'
export const QR_CODE_SCANNER_CLOSE_BUTTON = 'close-qr-scanner-icon'

// settings
// export const SETTINGS_PASS_CODE = 'settings-pass-code-label-touchable'
// export const SETTINGS_TOUCH_ID = 'settings-touch-id-label-touchable'
// export const SETTINGS_CHAT = 'settings-chat-id-label-touchable'
export const SETTINGS_CONTAINER = 'settings-container'
export const SETTINGS_HEADER = 'Settings'
export const SETTINGS_CREATE_BACKUP = 'Create a Backup'
export const BACKUP_CLOSE = 'recovery-header-close-image'
export const SETTINGS_BIOMETRICS = 'Biometrics'
export const BIOMETRICS_OK = 'Ok'
export const SETTINGS_PASSCODE = 'Passcode'
export const PASSCODE_BACK_ARROW = 'left-icon'
export const SETTINGS_CHAT = 'Give app feedback'
export const SETTINGS_ABOUT = 'About'
export const SETTINGS_ONFIDO = 'ONFIDO'

// switch environment screen
export const SWITCH_ENVIRONMENT_DEV_BUTTON = 'switch-environment-dev'
export const SWITCH_ENVIRONMENT_QA_TEST1_BUTTON = 'switch-environment-qatest1'
export const SWITCH_ENVIRONMENT_DEV_TEAM1_BUTTON = 'switch-environment-devteam1'
export const APP_ENVIRONMENT = `switch-environment-${
  process.env.environment || 'qa'
  // process.env.environment || 'devteam1'
  // 'devrc'
}`
export const SWITCH_ENVIRONMENT_SAVE_BUTTON = 'switch-environment-footer-accept'

// screenshot names
export const SCREENSHOT_HOME = 'home.png'
export const SCREENSHOT_HOME_SMALL_HISTORY = 'home2.png'
export const SCREENSHOT_HOME_BIG_HISTORY = 'home3.png'
export const SCREENSHOT_CONNECTIONS = 'connections.png'
export const SCREENSHOT_CREDENTIALS = 'credentials.png'
export const SCREENSHOT_SETTINGS = 'settings.png'
export const SCREENSHOT_MENU = 'menu.png'
export const SCREENSHOT_INVITATION = 'invitation.png'
export const SCREENSHOT_INVITATION_LINK_TO_EXISTING_CONNECTION =
  'invitation_link_to_existing_connection.png'
export const SCREENSHOT_CLAIM_OFFER_PROFILE_INFO = 'claim_offer_1.png'
export const SCREENSHOT_CLAIM_OFFER_ADDRESS = 'claim_offer_2.png'
export const SCREENSHOT_CLAIM_OFFER_CONTACT = 'claim_offer_3.png'
export const SCREENSHOT_CLAIM_OFFER_MIXED = 'claim_offer_4.png'
export const SCREENSHOT_PROOF_TEMPLATE_SINGLE_CLAIM_FULFILLED =
  'proof_request_1.png'
export const SCREENSHOT_PROOF_TEMPLATE_TWO_CLAIM_FULFILLED =
  'proof_request_2.png'
export const SCREENSHOT_TEST_CONNECTION = 'test_connection.png'
export const SCREENSHOT_EMPTY_CHAT = 'empty_chat.png'
export const SCREENSHOT_NOT_EMPTY_CHAT = 'not_empty_chat.png'
export const SCREENSHOT_ONFIDO_DOC_SELECTION = 'onfido_doc_selection.png'
export const SCREENSHOT_ONFIDO_PASSPORT = 'onfido_passport.png'
export const SCREENSHOT_ONFIDO_LICENSE = 'onfido_license.png'
export const SCREENSHOT_ONFIDO_NIC = 'onfido_nic.png'
export const SCREENSHOT_ONFIDO_RPC = 'onfido_rpc.png'
export const SCREENSHOT_ABOUT_MAIN = 'about_main.png'
export const SCREENSHOT_ABOUT_TAC = 'about_tac.png'
export const SCREENSHOT_ABOUT_PP = 'about_pp.png'
export const SCREENSHOT_MY_CREDENTIALS_LIST = 'my_credentials_list.png'
export const SCREENSHOT_MY_CREDENTIALS_ENTRY = 'my_credentials_entry.png'
export const SCREENSHOT_MY_CREDENTIALS_LIST_ONE_DELETED =
  'my_credentials_list_one_deleted.png'
export const SCREENSHOT_ALLOW_NOTIFICATIONS = 'allow_notifications.png'

// menu
export const MENU_CONTAINER = 'menu-container'
export const MENU_HOME = 'Home'
export const MENU_MY_CONNECTIONS = 'My Connections'
export const MENU_MY_CREDENTIALS = 'My Credentials'
export const MENU_SETTINGS = 'Settings'

// my connections
export const MY_CONNECTIONS_CONTAINER = 'my-connections-container'
export const MY_CONNECTIONS_HEADER = 'My Connections'
export const MY_CONNECTIONS_CONNECTION = 'Evernym QA-RC'
export const CONNECTION_ENTRY_HEADER = 'Added Connection'
export const VIEW_CREDENTIAL = 'VIEW CREDENTIAL'
export const CREDENTIAL_HEADER = 'Accepted Credential'
export const VIEW_PROOF = 'VIEW REQUEST DETAILS'
export const PROOF_HEADER = 'You shared this information'
export const CLOSE_BUTTON = 'Close'
export const CONNECTION_SUBMENU_BUTTON = 'three-dots'
export const CONNECTION_SUBMENU_CLOSE_BUTTON = 'delete-connection-close'
export const CONNECTION_DELETE_BUTTON = 'delete-connection'
export const PROOF_REQUEST_SCROLL_VIEW = 'RCTScrollContentView'
export const PROOF_REQUEST_ATTRIBUTE_INPUT = 'RCTUITextView'

// my credentials
export const MY_CREDENTIALS_HEADER = 'My Credentials'
export const MY_CREDENTIALS_DETAILS_HEADER = 'Credential Details'
export const MY_CREDENTIALS_DETAILS_ISSUED_BY = 'Issued by'
export const MY_CREDENTIALS_BACK_ARROW = 'left-icon'
export const MY_CREDENTIALS_DELETE = 'Delete'

// chat
export const CHAT_CANCEL = 'Cancel'
export const CHAT_CLOSE = 'Close'
export const CHAT_HEADER = 'Chat with Evernym'
export const CHAT_NEW_MESSAGE = 'New Message'
export const CHAT_TEXT_VIEW = 'UITextView'
export const CHAT_SEND_BUTTON = 'Send'
export const CHAT_SUCCESS_MESSAGE = 'We will respond to your message soon.'

// about
export const ABOUT_BACK_ARROW = 'left-icon'
export const ABOUT_HEADER = 'About this App'
export const ABOUT_TAC_BUTTON_HEADER = 'Terms and Conditions'
export const ABOUT_PP_BUTTON_HEADER = 'Privacy Policy'

// onfido
export const ONFIDO_BACK_ARROW = 'left-icon'
export const ONFIDO_CUSTOM_BACK_ARROW = 'back'
export const ONFIDO_ACCEPT_BUTTON = 'I accept'

// from api
export const CLAIM_OFFER_PROFILE_INFO = 'Profile Info'
export const CLAIM_OFFER_ADDRESS = 'Address'
export const PROOF_TEMPLATE_SINGLE_CLAIM_FULFILLED =
  'Automated Single claim fulfilled'
