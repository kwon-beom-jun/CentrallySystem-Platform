/**
 * Guide page i18n messages (English)
 */
export default {
  page: {
    title: 'Guide',
    subtitle: 'Guide page (Click image to zoom)',
    expandAll: 'Expand All',
    collapseAll: 'Collapse All',
  },

  sections: {
    signup: 'Sign Up',
    password: 'Password Recovery',
    social: 'Social Login',
    main: 'Main Page',
    notice: 'Notice',
    receipt: 'Receipt',
    favorite: 'Favorites',
    employee: 'Employee Management',
    system: 'System',
  },

  contact: {
    title: 'CONTACT',
    description: 'If you have any questions, please contact us',
    systemCategory: 'SYSTEM',
    managementCategory: 'MANAGEMENT',
    manager: 'Manager',
    email: 'Email',
    phone: 'Phone',
  },

  signup: {
    process: {
      summary: 'Sign-up Process',
      title: '📝 Full sign-up workflow',
      steps: [
        '<strong>Enter information</strong> → <strong>Send verification email</strong> → <strong>Verify email</strong> → <strong>Temporary registration</strong> → <strong>Admin approval</strong>',
        'Required data: name, phone number, email, password (confirmation), verification code, mandatory terms consent',
        'Administrator approval is required before full membership and permissions are granted.',
      ],
      flow: [
        'Enter information',
        'Send verification email',
        'Verify email',
        'Complete temporary registration',
        'Admin approval<br/>Grant permissions',
      ],
      tableTitle: '1️⃣ Information entry (table)',
      table: {
        head: ['Field', 'Required', 'Example'],
        rows: [
          { label: 'Name', required: '○', example: 'John Doe' },
          { label: 'Phone (mobile)', required: '○', example: '01012345678' },
          { label: 'Email', required: '○', example: 'user@example.com' },
          { label: 'Password · Confirm', required: '○', example: '••••••••' },
          { label: 'Mandatory terms consent', required: '○', example: 'Checked' },
        ],
      },
    },
    checklist: [
      'New users are registered as <strong>temporary members</strong><br/>└ Enter basic information (email · phone · department)',
      'Request approval directly from your immediate supervisor',
      'Supervisors approve new users, assign permissions/departments/teams, then convert them to full members',
      'Upon first login, review and edit personal information',
    ],
    images: {
      desktopAlt: 'Sign-up screen (desktop)',
      mobileAlt: 'Sign-up screen (mobile)',
    },
  },

  password: {
    steps: [
      'Enter email → receive a 6-digit verification code',
      'Upon successful code verification → temporary password sent via email',
      'After login go to “My Info” → change password menu to reset',
    ],
    images: {
      desktopAlts: ['Password recovery screen (desktop 1)', 'Password recovery screen (desktop 2)'],
      mobileAlts: ['Password recovery screen (mobile 1)', 'Password recovery screen (mobile 2)'],
    },
  },

  social: {
    steps: [
      'Complete <strong>pre-registration</strong> first using your company email address',
      'On the login screen choose the desired social option (<strong>Google/Kakao</strong>)',
      'Click <strong>“Confirm”</strong> in the social login popup',
      'After the popup closes, log in on the same page and verify the linkage in <strong>My Info</strong>',
    ],
    images: {
      desktopAlt: 'Social login screen (desktop)',
      mobileAlt: 'Social login screen (mobile)',
    },
  },

  main: {
    permissionsTitle: 'Required permission',
    permissions: ['Accessible to all logged-in users'],
    featuresTitle: 'Key features',
    features: [
      '<strong>Sidebar</strong>: Access every menu from the left navigation',
      '<strong>Theme switch</strong>: Choose light/dark theme from the top profile menu',
      '<strong>Favorites</strong>: Pin frequently used menus for quick access',
      '<strong>Latest notices</strong>: Preview the most recent three announcements',
      '<strong>Receipt requests</strong>: Check status of the latest three receipt submissions',
    ],
    images: {
      desktopAlt: 'Main page (desktop)',
      mobileAlt: 'Main page (mobile)',
    },
  },

  notice: {
    permissionsTitle: 'Required permission',
    permissions: [
      '<strong>General staff</strong>: View and search notices',
      '<strong>Middle managers</strong>: Create, edit, delete notices',
      '<strong>Administrators</strong>: All features plus pinning and audience control',
    ],
    featuresTitle: 'Key features',
    features: [
      'Text formatting: headings (1–3), bold, italic, bullet/numbered lists, code blocks',
      'Rich media: drag & drop images, attach PDFs, insert links',
      'Pin notices: turn on “Pin to top” so notices appear first on web and mobile',
      'Revision history: versions are recorded automatically when notices are updated',
    ],
    images: {
      desktopAlts: ['Notice screen (desktop 1)', 'Notice screen (desktop 2)'],
      mobileAlts: ['Notice screen (mobile 1)', 'Notice screen (mobile 2)'],
    },
  },

  receipt: {
    process: {
      summary: '🧾 Receipt processing guide',
      title: 'Overall workflow',
      flowSteps: ['Register receipt', 'Apply / edit / delete', 'Approval / agreement', 'Close'],
      exceptionStep: 'Exception handling',
      rejectFlow: ['🔁 Return: approval stage', '🔁 Return: closing stage'],
      sections: [
        {
          title: '1️⃣ Register receipt',
          items: [
            'New receipts start with status <strong>“Pending”</strong>.',
            '<strong>Location:</strong> `Receipts > Register Receipt > [Register Receipt] button`',
          ],
          table: {
            title: 'Required fields',
            head: ['Field', 'Description'],
            rows: [
              { label: 'Receipt image', description: 'Attach the original photo' },
              { label: 'Issued date', description: 'Date printed on the receipt' },
              { label: 'Category', description: 'Choose e.g., overtime meal, business trip' },
              { label: 'Approval line', description: 'Up to three approvers' },
              { label: 'Agreement users', description: 'No limit on participants' },
              { label: 'Usage reason', description: 'Enter the purpose' },
            ],
          },
        },
        {
          title: '2️⃣ Apply / edit / delete (pending)',
          items: [
            '<strong>Location:</strong> `Receipts > Register Receipt`',
            'Until submission you may <strong>edit</strong>, <strong>delete</strong>, or <strong>apply</strong> the receipt.',
            'Returned receipts revert to this stage so they can be <strong>resubmitted</strong>.',
          ],
        },
        {
          title: '3️⃣ Approval (including agreements)',
          items: [
            '<strong>Location:</strong><br/>• `Receipts > Approval Requests` → select row → detail → `[Approve]`<br/>• `Receipts > Agreement Requests` → select row → detail → `[Approve]`',
            '<strong>Visible to approvers:</strong> Only receipts where it is <strong>their turn</strong> in the approval line',
            '<strong>When returning:</strong> You must provide a <strong>return reason</strong>; it appears in the receipt history.',
          ],
        },
        {
          title: '4️⃣ Closing',
          items: [
            '<strong>Location:</strong> `Receipts > Close`',
            'After the CEO confirms approved receipts, the management team completes closing.',
            'Items returned by the CEO are marked as returned; approved ones are closed.',
          ],
        },
      ],
      exception: {
        title: '5️⃣ Exception handling',
        cases: [
          {
            title: '❗ When an approver loses permission',
            items: [
              'If an approver becomes inactive during approval, the receipt is <strong>automatically returned</strong> and the approver is noted in the reason.',
              'Example: `Automatically returned because approver John Doe became inactive`',
            ],
          },
          {
            title: '❗ Manual override by administrators',
            items: [
              'Administrators can <strong>change status manually</strong>; when they return a receipt their name is recorded in the return reason.',
            ],
          },
        ],
      },
      reference: {
        title: 'Reference',
        boxTitle: '✅ Category notes',
        items: [
          'Only administrators can create categories.',
          'Each category has a <strong>maximum claim amount</strong>; submissions exceeding it are rejected.',
        ],
      },
      summaryTable: {
        title: 'Summary',
        table: {
          head: ['Stage', 'Description'],
          rows: [
            { label: '1. Register', description: 'Enter required information and save with status `Pending`' },
            { label: '2. Apply/Edit/Delete', description: 'While `Pending`, you may apply, edit, or delete' },
            { label: '3. Approval/Agreement', description: 'Approvers/agreers approve or return; return reason required' },
            { label: '4. Close', description: 'CEO confirms approved items, then management finalizes closing' },
            { label: '5. Exception handling', description: 'Automatic returns for inactive users; administrators can override' },
          ],
        },
      },
    },
    permissionsTitle: 'Required permission',
    permissions: [
      '<strong>Registrant</strong>: Register and submit receipts',
      '<strong>Approver</strong>: Approve/return receipts in their queue',
      '<strong>Inspector</strong>: Review completed approvals',
      '<strong>Administrator</strong>: Access all receipts and settings',
    ],
    hierarchyTitle: 'Permission hierarchy',
    hierarchySubtitle: '[ Registrant ＜ Approver ＜ Inspector ＜ Finalizer ＜ Administrator ]',
    hierarchy: [
      '<strong>Registrant</strong>:<br/>&nbsp;• Upload receipt image, select agreement (approver) users<br/>&nbsp;• Track, delete, edit from “My Receipts” after submission<br/>&nbsp;• When returned, edit and resubmit',
      '<strong>Approver</strong>:<br/>&nbsp;• Can approve only receipts where it is <strong>their turn</strong><br/>&nbsp;• Review list and choose “Approve/Return”<br/>&nbsp;• Bulk approval available (including finalizers)',
      '<strong>Inspector</strong>:<br/>&nbsp;• Review approved receipts and return if issues exist<br/>&nbsp;• Export monthly Excel report → confirm with supervisor → mark as closed',
      '<strong>Administrator</strong>:<br/>&nbsp;• Access every receipt and force status changes<br/>&nbsp;• Manage default approval lines, categories, spending limits',
    ],
    processListTitle: 'Process',
    processList: [
      'Registrant: upload receipts & configure approval line',
      'Approver: approve items individually or in bulk',
      'Inspector: review approved items & include them in statistics',
      'Closing: after inspection, close monthly data → integrate with accounting',
    ],
    images: {
      desktopAlts: [
        'Receipt registration screen (desktop)',
        'Receipt registration modal (desktop)',
        'Receipt edit screen (desktop)',
      ],
      mobileAlts: [
        'Receipt registration screen (mobile)',
        'Receipt registration modal (mobile)',
        'Receipt edit screen (mobile)',
      ],
    },
  },

  favorite: {
    permissionsTitle: 'Required permission',
    permissions: ['Accessible to all logged-in users'],
    featuresTitle: 'Key features',
    features: [
      '<strong>Add favorites</strong>: Pin frequently used menus',
      '<strong>Card design</strong>: Choose from five designs on mobile',
      '<strong>Reorder</strong>: Drag and drop to change menu order',
      '<strong>Remove favorites</strong>: Delete menus that are no longer needed',
    ],
    images: {
      desktopAlts: ['Favorites screen (desktop 1)', 'Favorites card design modal (desktop)'],
      mobileAlts: ['Favorites screen (mobile 1)', 'Favorites card design modal (mobile)'],
    },
  },

  employee: {
    permissionsTitle: 'Required permission',
    permissions: [
      '<strong>Middle managers</strong>: Approve temporary members, assign permissions, manage departments/teams',
      '<strong>Administrators</strong>: Register/edit/view/delete employee info, manage permissions, maintain org chart',
    ],
    featuresTitle: 'Key features',
    features: [
      'Approve temporary members: grant permissions to new users',
      'Department/team management: build and maintain the organization tree',
      'Permission management: configure and update user permissions',
      'Org chart: reorganize departments/teams via drag and drop',
    ],
    images: {
      desktopAlts: ['Permission assignment screen (desktop)', 'Department/Team management screen (desktop)'],
      mobileAlts: ['Permission assignment screen (mobile)', 'Department/Team management screen (mobile)'],
    },
  },

  system: {
    permissionsTitle: 'Required permission',
    permissions: ['Accessible only to <strong>system administrators</strong>'],
    featuresTitle: 'Key features',
    features: [
      '<strong>System settings</strong>: Configure and monitor every module',
      '<strong>Audit logs</strong>: Review user, API call, and data change history',
      '<strong>Dashboard</strong>: Charts for headcount, receipts, department spending with period/department filters',
    ],
    images: {
      desktopAlts: ['Dashboard screen (desktop)'],
      mobileAlts: ['Dashboard screen (mobile)'],
    },
  },
}

