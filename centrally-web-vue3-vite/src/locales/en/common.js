/**
 * Common i18n messages (English)
 */
export default {
  // Common buttons and actions
  button: {
    confirm: 'Confirm',
    cancel: 'Cancel',
    save: 'Save',
    delete: 'Delete',
    edit: 'Edit',
    add: 'Add',
    search: 'Search',
    reset: 'Reset',
    close: 'Close',
    submit: 'Submit',
    approve: 'Approve',
    reject: 'Reject',
    register: 'Register',
    modify: 'Modify',
    download: 'Download',
    upload: 'Upload',
    detail: 'Detail',
    list: 'List',
    create: 'Create',
    update: 'Update',
    refresh: 'Refresh',
    back: 'Back',
    next: 'Next',
    prev: 'Previous',
    apply: 'Apply',
    select: 'Select',
    selectAll: 'Select All',
    deselectAll: 'Deselect All',
    expand: 'Expand',
    collapse: 'Collapse',
  },

  // Common labels
  label: {
    name: 'Name',
    email: 'Email',
    phone: 'Phone',
    department: 'Department',
    team: 'Team',
    position: 'Position',
    rank: 'Rank',
    status: 'Status',
    date: 'Date',
    startDate: 'Start Date',
    endDate: 'End Date',
    createdAt: 'Created',
    updatedAt: 'Updated',
    author: 'Author',
    title: 'Title',
    content: 'Content',
    description: 'Description',
    file: 'File',
    image: 'Image',
    category: 'Category',
    type: 'Type',
    count: 'Count',
    total: 'Total',
    all: 'All',
    none: 'None',
    unspecified: 'Unspecified',
    search: 'Search',
    filter: 'Filter',
    sort: 'Sort',
    order: 'Order',
    page: 'Page',
    default: 'Default',
    style1: 'Style 1',
    style2: 'Style 2',
    style3: 'Style 3',
    style4: 'Style 4',
    selection: 'Selection',
    select: 'Select',
  },

  // Common status
  status: {
    active: 'Active',
    inactive: 'Inactive',
    pending: 'Pending',
    approved: 'Approved',
    rejected: 'Rejected',
    completed: 'Completed',
    inProgress: 'In Progress',
    draft: 'Draft',
    published: 'Published',
    deleted: 'Deleted',
    enabled: 'Enabled',
    disabled: 'Disabled',
  },

  // Common messages
  message: {
    loading: 'Loading...',
    noData: 'No data available',
    saved: 'Saved successfully',
    deleted: 'Deleted successfully',
    updated: 'Updated successfully',
    created: 'Created successfully',
    submitted: 'Submitted successfully',
    approved: 'Approved successfully',
    rejected: 'Rejected successfully',
    copied: 'Copied to clipboard',
    downloadSuccess: 'Downloaded successfully',
    uploadSuccess: 'Uploaded successfully',
    required: 'This field is required',
    invalidFormat: 'Invalid format',
    confirmDelete: 'Are you sure you want to delete?',
    confirmSave: 'Do you want to save?',
    confirmCancel: 'Are you sure you want to cancel? Your changes will be lost',
    loginRequired: 'Login required',
    permissionDenied: 'Permission denied',
    sessionExpired: 'Session expired',
    networkError: 'Network connection failed',
    serverError: 'Server error occurred',
    unknownError: 'An unknown error occurred',
  },

  // Common placeholders
  placeholder: {
    search: 'Enter search term',
    select: 'Please select',
    input: 'Please enter',
    email: 'Enter email',
    phone: 'Enter phone number',
    name: 'Enter name',
    title: 'Enter title',
    content: 'Enter content',
    date: 'Select date',
  },

  // Common validation messages
  validation: {
    required: '{field} is required',
    email: 'Invalid email format',
    phone: 'Invalid phone number format',
    minLength: '{field} must be at least {min} characters',
    maxLength: '{field} must be at most {max} characters',
    number: 'Only numbers are allowed',
    onlyKorean: 'Only Korean characters are allowed',
    onlyEnglish: 'Only English characters are allowed',
    password: 'Password must contain letters, numbers, and special characters',
    passwordMismatch: 'Passwords do not match',
  },

  // Company info modal
  companyInfo: {
    logoAlt: 'Company logo',
    companyName: 'Company Name',
    address: 'Address',
    phone: 'Phone',
    fax: 'Fax',
    email: 'Email',
    homepage: 'Homepage',
    ceo: 'CEO',
    businessAreas: 'Business Areas',
  },

  // Weekday abbreviations
  weekday: {
    sun: 'Sun', mon: 'Mon', tue: 'Tue', wed: 'Wed', thu: 'Thu', fri: 'Fri', sat: 'Sat',
  },

  // Sidebar
  sidebar: {
    workspace: {
      home: 'Home', info: 'Info', receipt: 'Receipt', management: 'HRM', user: 'Profile', system: 'System', favorites: 'Favorites'
    },
    tooltip: {
      toDark: 'Switch to dark mode',
      toLight: 'Switch to light mode',
      companyInfo: 'Company Info',
    },
    message: {
      themeChanged: 'Theme has been changed.',
    },
    controls: {
      collapseAll: 'Collapse all',
      expandAll: 'Expand all',
      collapseSidebar: 'Collapse sidebar',
      expandSidebar: 'Expand sidebar',
    },
    category: {
      scheduleManagement: 'Schedule Management',
      board: 'Board',
      guide: 'Guide',
    },
    receipt: {
      registrar: 'Registrant',
      approver: 'Approver',
      proxy: 'Proxy Approver',
      inspector: 'Inspector',
      manager: 'Manager',
    },
    hrm: {
      userAuth: 'Users & Access',
      organization: 'Organization',
      performance: 'Performance',
      approval: 'Join Approval',
    },
    user: {
      personal: 'Personal Info',
      myInfo: 'My Info',
      profileImageAlt: 'Profile image',
      defaultName: 'User',
    },
    system: {
      admin: 'System Admin',
    },
    search: {
      title: 'Employee Search',
      placeholder: 'Search by name or email',
      loading: 'Searching...',
      noResults: 'No results found',
      clearSelection: 'Clear selection',
    },
    favorites: {
      emptyTitle: 'No favorite menus',
      emptyDesc: 'Add frequently used menus to Favorites',
      manage: 'Manage Favorites',
      infoLine1: 'Favorites are stored on the server',
      infoLine2: 'and synced across all your devices',
    },
    favorite: {
      add: 'Add to favorites',
      removeTitle: 'Remove Favorite',
    },
    time: {
      dateFormat: '{year}-{month}-{date} {day}',
    },
    activity: {
      recentHistory: 'Recent Activity',
      noHistory: 'No activity history',
      unknownAction: 'Unknown action',
      unclassified: 'Unclassified',
      timeAgo: {
        justNow: 'Just now',
        minutesAgo: '{minutes} minutes ago',
        hoursAgo: '{hours} hours ago',
        daysAgo: '{days} days ago',
      },
      service: {
        auth: 'Auth',
        hrm: 'HRM',
        info: 'Info',
        receipt: 'Receipt',
        system: 'System',
        common: 'Common',
        default: 'Other',
      },
    },
  },

  // Footer
  footer: {
    copyright: 'CENTRALLY SYSTEM. Copyright 2025 KBJ. All rights reserved.',
  },

  // Mobile Drawer Menu
  mobileDrawer: {
    nameUnspecified: 'Name not specified',
    emailUnspecified: 'Email not specified',
    positionUnspecified: 'Position not specified',
    profileAlt: 'Profile',
    noMenu: 'No menu',
    theme: {
      switchToDark: 'Switch to dark mode',
      switchToLight: 'Switch to light mode',
      changed: 'Theme has been changed.',
      changeFailed: 'Failed to change theme.',
    },
  },

  // Navigation Layout
  navigation: {
    notice: 'Notice',
    receiptRegister: 'Receipt Entry',
    receiptHistory: 'Receipt History',
    more: 'More',
    write: 'Write',
    receiptApproval: 'Receipt Approve',
    quickMenu: {
      empty: 'No quick menu available.',
    },
    receipt: {
      noPermission: 'No permission to register receipts',
      movedToPage: 'Moved to receipt registration page.',
    },
  },

  // Notification
  notification: {
    title: 'Notification',
    approval: 'Approval',
    concurrence: 'Concurrence',
    approvalWaiting: 'Approval Waiting {count}',
    concurrenceRequest: 'Concurrence Request {count}',
    summary: 'Approvals {approvals} · Concurrences {concurrences}',
  },

  // Card
  card: {
    participants: 'Participants',
    typeReason: 'Type/Reason',
    amount: 'Amount',
    amountPerPerson: 'Amount/People',
    receiptPhoto: 'Receipt Photo',
    peopleList: 'List',
    people: 'People',
    peopleCount: 'People Count',
  },

  // Mobile User Card
  mobileUserCard: {
    favorites: 'Favorites',
    manage: 'Manage',
    user: 'User',
    departmentUnspecified: 'Department not specified',
    positionUnspecified: 'Position not specified',
    greeting: 'Hello 👋',
    greetingSimple: 'Hello!',
    noFavorites: 'No favorites registered',
    addFavoritesHint: 'Click the settings button to add',
    registerFavorites: 'Register frequently used menus',
    addFavorites: 'Add frequently used menus',
    quickMenu: 'Quick Menu',
    addFavoritesGuide: 'Add favorites',
    addFavoritesGuideHint: 'Click the ⚙️ button in the top right to register',
    settings: 'Settings',
    profileAlt: 'Profile',
  },

  // Address Search
  address: {
    search: 'Address Search',
  },

  // Pagination
  pagination: {
    prev: 'Previous',
    next: 'Next',
  },

  // Guide Helper
  guide: {
    title: 'Guide',
    clickHint: 'If you have any questions,<br>click me',
  },
}
