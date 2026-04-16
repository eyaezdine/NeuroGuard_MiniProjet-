export interface NavigationItem {
  id: string;
  title: string;
  type: 'item' | 'collapse' | 'group';
  translate?: string;
  icon?: string;
  hidden?: boolean;
  url?: string;
  classes?: string;
  groupClasses?: string;
  exactMatch?: boolean;
  external?: boolean;
  target?: boolean;
  breadcrumbs?: boolean;
  children?: NavigationItem[];
  link?: string;
  description?: string;
  path?: string;
}

export const NavigationItems: NavigationItem[] = [
  {
    id: 'dashboard',
    title: 'Dashboard',
    type: 'group',
    icon: 'icon-navigation',
    children: [
      {
        id: 'default',
        title: 'Home',
        type: 'item',
        classes: 'nav-item',
        url: '/dashboard/default',
        icon: 'dashboard',
        breadcrumbs: false
      }
    ]
  },
  {
    id: 'microservices',
    title: 'Microservices',
    type: 'group',
    icon: 'icon-navigation',
    children: [
      {
        id: 'user-management',
        title: 'User Management',
        type: 'item',
        classes: 'nav-item',
        url: '/ms/users',
        icon: 'user',
        breadcrumbs: true
      },
      {
        id: 'medical-history',
        title: 'Medical History',
        type: 'item',
        classes: 'nav-item',
        url: '/ms/medical-history',
        icon: 'medicine-box',
        breadcrumbs: true
      },
      {
        id: 'consultation',
        title: 'Consultation',
        type: 'item',
        classes: 'nav-item',
        url: '/ms/consultation',
        icon: 'solution',
        breadcrumbs: true
      },
      {
        id: 'forum',
        title: 'Forum',
        type: 'item',
        classes: 'nav-item',
        url: '/ms/forum',
        icon: 'message',
        breadcrumbs: true
      },
      {
        id: 'wellbeing',
        title: 'Well-being',
        type: 'item',
        classes: 'nav-item',
        url: '/ms/wellbeing',
        icon: 'heart',
        breadcrumbs: true
      },
      {
        id: 'delivery',
        title: 'Delivery',
        type: 'item',
        classes: 'nav-item',
        url: '/ms/delivery',
        icon: 'car',
        breadcrumbs: true
      }
    ]
  },
  {
    id: 'authentication',
    title: 'Authentication',
    type: 'group',
    icon: 'icon-navigation',
    children: [
      {
        id: 'login',
        title: 'Login',
        type: 'item',
        classes: 'nav-item',
        url: '/login',
        icon: 'login',
        target: true,
        breadcrumbs: false
      },
      {
        id: 'register',
        title: 'Register',
        type: 'item',
        classes: 'nav-item',
        url: '/register',
        icon: 'profile',
        target: true,
        breadcrumbs: false
      }
    ]
  }
];

