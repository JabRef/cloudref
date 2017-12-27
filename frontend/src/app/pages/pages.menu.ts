export const PAGES_MENU = [
  {
    path: '',
    children: [
      {
        path: 'references',
        data: {
          menu: {
            title: 'All references',
            icon: 'fa fa-table',
            pathMatch: 'prefix', // use it if item children not displayed in menu
            selected: false,
            expanded: false,
            order: 0
          }
        }
      },
      {
        path: 'import',
        data: {
          menu: {
            title: 'Add references',
            icon: 'fa fa-plus-square',
            pathMatch: 'prefix', // use it if item children not displayed in menu
            selected: false,
            expanded: false,
            order: 100,
          }
        }
      },
    ]
  }
];
