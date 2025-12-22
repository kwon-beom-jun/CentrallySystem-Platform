/**
 * Info service i18n messages (English)
 * Naming: info.page.action/status
 */
export default {
  // Notice board
  notice: {
    title: 'Notice',
    list: 'Notice List',
    detail: 'Notice Detail',
    create: 'Create Notice',
    edit: 'Edit Notice',
    delete: 'Delete Notice',
    subject: 'Subject',
    content: 'Content',
    author: 'Author',
    createdAt: 'Created',
    views: 'Views',
    attachments: 'Attachments',
    important: 'Important',
    pinned: 'Pinned',
    saveSuccess: 'Saved successfully',
    deleteSuccess: 'Deleted successfully',
    deleteConfirm: 'Are you sure you want to delete?',
    noNotices: 'No notices',
  },

  // Community board
  community: {
    title: 'Community',
    list: 'Post List',
    detail: 'Post Detail',
    create: 'Create Post',
    edit: 'Edit Post',
    delete: 'Delete Post',
    subject: 'Subject',
    content: 'Content',
    author: 'Author',
    createdAt: 'Created',
    views: 'Views',
    likes: 'Likes',
    comments: 'Comments',
    attachments: 'Attachments',
    category: 'Category',
    saveSuccess: 'Saved successfully',
    deleteSuccess: 'Deleted successfully',
    deleteConfirm: 'Are you sure you want to delete?',
    noPosts: 'No posts',
    like: 'Like',
    unlike: 'Unlike',
  },

  // Resource board
  resource: {
    title: 'Resources',
    list: 'Resource List',
    detail: 'Resource Detail',
    create: 'Upload Resource',
    edit: 'Edit Resource',
    delete: 'Delete Resource',
    subject: 'Subject',
    content: 'Content',
    author: 'Author',
    createdAt: 'Created',
    views: 'Views',
    downloads: 'Downloads',
    attachments: 'Attachments',
    category: 'Category',
    fileSize: 'File Size',
    download: 'Download',
    saveSuccess: 'Saved successfully',
    deleteSuccess: 'Deleted successfully',
    deleteConfirm: 'Are you sure you want to delete?',
    noResources: 'No resources',
    downloadSuccess: 'Downloaded successfully',
    downloadFailed: 'Download failed',
  },

  // Comments
  comment: {
    add: 'Add Comment',
    edit: 'Edit Comment',
    delete: 'Delete Comment',
    reply: 'Reply',
    content: 'Comment',
    placeholder: 'Enter your comment',
    saveSuccess: 'Comment added',
    deleteSuccess: 'Comment deleted',
    deleteConfirm: 'Are you sure you want to delete?',
    noComments: 'No comments',
  },

  // Attachments
  attachment: {
    add: 'Attach File',
    delete: 'Delete File',
    download: 'Download',
    noFiles: 'No attachments',
    maxSize: 'Max file size',
    allowedTypes: 'Allowed file types',
    uploadSuccess: 'File uploaded',
    uploadFailed: 'File upload failed',
    deleteSuccess: 'File deleted',
  },

  // Common (Board)
  board: {
    title: {
      notice: 'Notice',
      community: 'Community Board',
      resource: 'Resource Board',
    },
    createPost: 'Create Post',
    editPost: 'Edit Post',
    noPosts: 'No posts',
    unknown: 'Unknown',
    columns: {
      id: 'No.',
      title: 'Title',
      content: 'Content',
      author: 'Author',
      editor: 'Editor',
      date: 'Date',
    },
    mobile: {
      postNumber: 'Post No.',
    },
  },

  // Mobile Post Create/Edit
  postMobile: {
    title: {
      notice: 'Create Notice',
      resource: 'Upload Resource',
      community: 'Create Post',
      default: 'Create Post',
    },
    editTitle: {
      notice: 'Edit Notice',
      resource: 'Edit Resource',
      community: 'Edit Post',
      default: 'Edit Post',
    },
    labels: {
      title: 'Title',
      visibilityScope: 'Visibility Scope',
      mailSend: 'Send Mail',
      content: 'Content',
      attachment: 'Attachments',
    },
    placeholders: {
      title: 'Enter title',
    },
    mail: {
      all: 'All',
      department: 'Department',
      team: 'Team',
      guest: 'Guest',
      user: 'User',
      manager: 'Manager',
      admin: 'Admin',
      selectDepartment: 'Select Department',
      selectTeam: 'Select Team',
      multipleSelect: 'Multiple selections allowed.',
    },
    file: {
      select: 'Select File',
      multipleSelect: 'Multiple files allowed',
      new: 'New',
      existing: 'Existing',
    },
    buttons: {
      goBack: 'Back',
      create: 'Create',
      edit: 'Edit',
    },
    validation: {
      titleRequired: 'Please enter title',
      contentRequired: 'Please enter content',
    },
    success: {
      create: 'Post created successfully',
      edit: 'Post updated successfully',
    },
    visibility: {
      all: 'All (including GUEST)',
      userAbove: 'USER and above',
      managerAbove: 'MANAGER and above',
    },
  },

  // Post Create/Edit Modal
  postModal: {
    title: {
      notice: 'Notice',
      resource: 'Resource',
      community: 'Community Board',
      default: 'Post',
    },
    labels: {
      title: 'Title',
      visibilityScope: 'Visibility Scope',
      mailSend: 'Send Mail',
      author: 'Author',
      date: 'Date',
      content: 'Content',
      existingAttachments: 'Existing Attachments',
      newFile: 'Add New File',
      attachment: 'Attachments',
      noAttachment: 'No attachments',
    },
    placeholders: {
      title: 'Enter title',
      author: 'Auto-filled',
    },
    buttons: {
      collapse: 'Collapse',
      expand: 'Expand',
      close: 'Close',
      create: 'Create',
      edit: 'Edit',
    },
    validation: {
      titleRequired: 'Please enter title',
      contentRequired: 'Please enter content',
      contentSizeExceeded: 'Content cannot exceed 5 MB.',
    },
    success: {
      create: 'Post created successfully',
      edit: 'Post updated successfully',
    },
    confirm: {
      closeTitle: 'Confirm',
      closeMessage: 'Do you want to close the post you are writing?',
    },
  },

  // Post Detail
  postDetail: {
    loading: 'Loading post...',
    labels: {
      content: 'Content',
      attachment: 'Attachments',
      download: '[Download]',
    },
    buttons: {
      unpin: 'Unpin',
      pin: 'Pin Post',
      edit: 'Edit',
      delete: 'Delete',
    },
    comments: {
      title: 'Comments',
      placeholder: 'Enter your comment',
      replyPlaceholder: 'Enter your reply',
      reply: 'Reply',
      edit: 'Edit',
      delete: 'Delete',
      save: 'Save',
      cancel: 'Cancel',
      register: 'Register',
      count: '{count} comments',
    },
    confirm: {
      deleteTitle: 'Delete Confirmation',
      deleteMessage: 'Are you sure you want to delete?',
    },
    success: {
      delete: 'Post deleted successfully',
      pin: 'Post has been pinned.',
      unpin: 'Post has been unpinned.',
    },
    error: {
      loadFailed: 'Failed to load post information.',
    },
  },
}

