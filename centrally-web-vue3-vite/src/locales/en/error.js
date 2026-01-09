/**
 * Error messages (English)
 */
export default {
  // HTTP status code errors
  http: {
    400: 'Bad request',
    401: 'Authentication required',
    403: 'Access denied',
    404: 'Page not found',
    405: 'Method not allowed',
    408: 'Request timeout',
    409: 'Request conflict',
    500: 'Internal server error',
    502: 'Bad gateway',
    503: 'Service unavailable',
    504: 'Gateway timeout',
  },

  // Service-specific errors
  service: {
    auth: {
      connection: '[A] Failed to connect to authentication service',
      unknown: '[A] An unknown error occurred',
      unauthorized: 'You have been logged out\nPlease login again',
      roleChanged: 'Your role has changed\nPlease login again',
      forbidden: '[A] Access denied',
      unavailable: '[A] Service unavailable',
    },
    hrm: {
      connection: '[H] Failed to connect to HR service',
      unknown: '[H] An unknown error occurred',
      forbidden: '[H] Access denied',
      unavailable: '[H] Service unavailable',
    },
    receipt: {
      connection: '[R] Failed to connect to receipt service',
      unknown: '[R] An unknown error occurred',
      forbidden: '[R] Access denied',
      unavailable: '[R] Service unavailable',
    },
    info: {
      connection: '[I] Failed to connect to information service',
      unknown: '[I] An unknown error occurred',
      forbidden: '[I] Access denied',
      unavailable: '[I] Service unavailable',
    },
    system: {
      connection: '[S] Failed to connect to system service',
      unknown: '[S] An unknown error occurred',
      forbidden: '[S] Access denied',
      unavailable: '[S] Service unavailable',
    },
  },

  // Network errors
  network: {
    offline: 'Internet connection lost',
    timeout: 'Request timeout',
    abort: 'Request cancelled',
  },

  // Validation errors
  validation: {
    required: 'This field is required',
    email: 'Invalid email format',
    phone: 'Invalid phone number format',
    password: 'Password must be at least 8 characters and contain letters, numbers, and special characters',
    passwordMismatch: 'Passwords do not match',
    minLength: 'Please enter at least {min} characters',
    maxLength: 'Maximum {max} characters allowed',
    fileSize: 'File size must be less than {size}MB',
    fileType: 'Unsupported file type',
  },

  // File-related errors
  file: {
    uploadFailed: 'File upload failed',
    downloadFailed: 'File download failed',
    sizeExceeded: 'File size is too large',
    typeNotSupported: 'Unsupported file type',
    tooManyFiles: 'Too many files',
  },
}

