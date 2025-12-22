/**
 * Auth service i18n messages (English)
 * Naming: auth.page.action/status
 */
export default {
  // Login page
  login: {
    title: 'LOGIN',
    systemTitle: 'Centrally System',
    systemSubtitle: 'centrally system is a company management system',
    accountLabel: 'Account',
    passwordLabel: 'Password',
    email: 'Email',
    password: 'Password',
    emailPlaceholder: 'Enter email',
    passwordPlaceholder: 'Enter password',
    rememberMe: 'Remember ID',
    forgotPassword: 'Forgot login password?',
    noAccount: "Don't have an account?",
    signUp: 'Sign up',
    loginButton: 'Login',
    loginSuccess: 'Logged out successfully',
    loginFailed: 'Login failed',
    emailRequired: 'Please enter your ID and password',
    passwordRequired: 'Please enter your password',
    invalidCredentials: 'Invalid email or password',
    accountLocked: 'Account locked. Please contact administrator',
    socialLogin: 'Social Login',
    socialUnknown: 'Unknown social login method.',
    ipWarning: 'Please note that IP information is recorded from login. If you do not wish this, please do not use this service',
    socialModalTitle: 'Social Login',
    socialModalMessage: 'Social authentication completed.\nNo account is linked to this social account.\nPlease login to complete the linkage\n(Expires in 5 minutes)',
    socialModalFooter: 'If you do not have an account to link,\nor do not want to link, click "Cancel" below',
  },

  // Sign up page
  join: {
    title: 'JOIN',
    systemTitle: 'Centrally System',
    systemSubtitle: 'centrally system is a company management system',
    name: 'Name',
    phone: 'Phone',
    phonePlaceholder: "Enter without '-'",
    email: 'Email',
    password: 'Password',
    passwordConfirm: 'Confirm Password',
    verificationCode: 'Verification Code',
    verificationCodePlaceholder: 'Verification Code',
    sendVerificationEmail: 'Send Email',
    verifyButton: 'Verify',
    agreeTerms: 'I agree to the terms of service',
    agreePrivacy: 'I agree to the privacy policy',
    signUpButton: 'Sign Up',
    backButton: 'Back',
    signUpSuccess: 'Sign up completed',
    signUpFailed: 'Sign up failed',
    emailRequired: 'Please enter your email',
    emailDuplicate: 'Email is already in use',
    passwordMismatch: 'Passwords do not match',
    verificationCodeRequired: 'Please enter verification code',
    verificationRequired: 'Please complete email verification',
    termsRequired: 'Please agree to the required terms',
    privacyRequired: 'Please agree to the privacy policy',
    hasAccount: 'Already have an account?',
    goToLogin: 'Login',
    agreementModalTitle: 'Terms Agreement',
    agreementRequired: 'Required',
    agreementOptional: 'Optional',
    agreementAgreeAll: 'Agree to All',
    agreementCompleted: 'Required terms agreed',
    agreementRequired2: 'Required terms agreement needed',
  },

  // OAuth callback
  oauth: {
    processing: 'Processing OAuth authentication...',
    success: 'OAuth authentication completed',
    failed: 'Social Login Failed',
    cancelled: 'OAuth authentication cancelled',
    duplicateLogin: 'A problem occurred due to duplicate login.\nPlease login again',
  },

  // Temporary user approvals
  tempApprovals: {
    title: 'Temporary User Management',
    subtitle: 'Manage temporary users pending approval.',
    list: 'Pending Approval List',
    nameLabel: 'Name:',
    searchPlaceholder: 'Search by name or email',
    approve: 'Approve',
    reject: 'Reject',
    approveSuccess: 'Approved successfully',
    rejectSuccess: 'Rejected successfully',
    approveSuccessMessage: '{name} approved',
    rejectSuccessMessage: '{name} deleted',
    approveConfirm: 'Do you want to approve?',
    rejectConfirm: 'Do you want to reject?',
    deleteConfirmTitle: 'Delete Confirmation',
    deleteConfirmMessage: 'Are you sure you want to delete {name}?',
    noRequests: 'No pending approval requests',
  },

  // Logout
  logout: {
    confirm: 'Do you want to logout?',
    success: 'Logged out successfully',
    failed: 'Logout failed',
  },

  // Session management
  session: {
    expired: 'Session expired. Please login again',
    invalid: 'Invalid session',
    extended: 'Session extended',
  },

  // Permission grant modal
  permission: {
    title: 'Grant Permission',
    userSearch: 'User Search',
    selectedUser: 'Selected User',
    service: 'Service',
    role: 'Permission',
    allServiceGranted: 'All service permissions have been added',
    serviceSelect: 'Select Service',
    roleSelect: 'Select Permission',
    selectAllItems: 'Please select all items.',
    addSuccess: 'Permission has been added.',
  },

  // Forgot password modal
  forgotPassword: {
    title: 'Password Reset',
    subtitle: 'Email verification sends a temporary password afterward',
    emailPlaceholder: 'Registered Email',
    codePlaceholder: '6-digit Verification Code',
    guideText: 'Please enter the code within 5 minutes after sending the verification email.',
    sendCode: 'Send Verification Email',
    verifyCode: 'Verify',
    emailRequired: 'Please enter your email',
    emailFormat: 'Please enter a valid email format',
    emailAndCodeRequired: 'Please enter both email and verification code',
    verifySuccess: '[ Verification Complete ]\nA temporary password has been sent to your email.',
  },

  // Password verification modal
  passwordVerification: {
    title: 'Password Verification',
    message1: 'For privacy protection,',
    message2: 'please enter your password',
    currentPassword: 'Current Password',
    currentPasswordPlaceholder: 'Enter your current password',
    passwordRequired: 'Please enter your password.',
    verifySuccess: 'Password verified successfully.',
    passwordMismatch: 'Password does not match.',
  },

  // Password modify modal
  passwordModify: {
    title: 'Change Password',
    currentPassword: 'Current Password',
    newPassword: 'New Password',
    confirmPassword: 'Confirm Password',
    currentPasswordPlaceholder: 'Enter your current password',
    newPasswordPlaceholder: 'Enter your new password',
    confirmPasswordPlaceholder: 'Re-enter your new password',
    passwordMismatch: 'New passwords do not match.',
    currentPasswordRequired: 'Please enter your current password.',
    newPasswordRequired: 'Please enter both new password and confirmation.',
    changeSuccess: 'Password has been changed successfully.',
  },

  // Temporary user detail modal
  tempUser: {
    title: 'Temporary User Detail',
    name: 'Name',
    email: 'Email',
    phone: 'Phone',
    approve: 'Approve',
    delete: 'Delete',
  },

  // User search dropdown
  userSearch: {
    placeholder: 'Search by name (email)',
    excludeSelf: 'Exclude self',
    noResults: 'No search results',
  },
}

