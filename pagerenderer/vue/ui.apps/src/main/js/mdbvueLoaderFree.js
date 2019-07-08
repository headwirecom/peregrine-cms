import {
  mdbAccordion,
  mdbAccordionPane,
  mdbAlert,
  mdbBadge,
  mdbBreadcrumb,
  mdbBreadcrumbItem,
  mdbBtn,
  mdbBtnGroup,
  mdbBtnToolbar,
  mdbCard,
  mdbCardAvatar,
  mdbCardBody,
  mdbCardFooter,
  mdbCardGroup,
  mdbCardHeader,
  mdbCardImage,
  mdbCardText,
  mdbCardTitle,
  mdbCardUp,
  mdbCarousel,
  mdbCarouselCaption,
  mdbCarouselControl,
  mdbCarouselIndicator,
  mdbCarouselIndicators,
  mdbCarouselInner,
  mdbCarouselItem,
  mdbBarChart,
  mdbDoughnutChart,
  mdbLineChart,
  mdbPieChart,
  mdbPolarChart,
  mdbRadarChart,
  mdbBubbleChart,
  mdbScatterChart,
  mdbHorizontalBarChart,
  mdbCol,
  mdbCollapse,
  mdbContainer,
  mdbDatatable,
  mdbDropdown,
  mdbDropdownItem,
  mdbDropdownMenu,
  mdbDropdownToggle,
  mdbEdgeHeader,
  mdbIcon,
  mdbInput,
  mdbFooter,
  mdbFormInline,
  mdbGoogleMap,
  mdbJumbotron,
  mdbListGroup,
  mdbListGroupItem,
  mdbMask,
  mdbMedia,
  mdbMediaBody,
  mdbMediaImage,
  mdbModal,
  mdbModalHeader,
  mdbModalTitle,
  mdbModalBody,
  mdbModalFooter,
  mdbNumericInput,
  mdbNavbar,
  mdbNavbarBrand,
  mdbNavbarNav,
  mdbNavbarToggler,
  mdbNavItem,
  mdbPageItem,
  mdbPageNav,
  mdbPagination,
  mdbPopover,
  mdbProgress,
  mdbRow,
  mdbTabs,
  mdbTab,
  mdbTabContent,
  mdbTabItem,
  mdbTabPane,
  mdbTbl,
  mdbTblBody,
  mdbTblHead,
  mdbTextarea,
  mdbTooltip,
  mdbView,
  mdbScrollbar,
  mdbStretchedLink,
  mdbToastNotification,
  mdbMasonry,
  mdbMasonryItem,

  waves,
  mdbClassMixin,
  animateOnScroll
} from 'mdbvue';
//>-CUSTOM-COMPONENTS-IMPORT-<//

const mdbvue = {
  load: function (Vue) {

    //>-CUSTOM-COMPONENTS-<//

    Vue.component('mdb-accordion', mdbAccordion);
    Vue.component('mdb-accordion-pane', mdbAccordionPane);
    Vue.component('mdb-alert', mdbAlert);
    Vue.component('mdb-badge', mdbBadge);
    Vue.component('mdb-breadcrumb', mdbBreadcrumb);
    Vue.component('mdb-breadcrumb-item', mdbBreadcrumbItem);
    Vue.component('mdb-btn', mdbBtn);
    Vue.component('mdb-btn-group', mdbBtnGroup);
    Vue.component('mdb-btn-toolbar', mdbBtnToolbar);
    Vue.component('mdb-card', mdbCard);
    Vue.component('mdb-card-avatar', mdbCardAvatar);
    Vue.component('mdb-card-up', mdbCardUp);
    Vue.component('mdb-card-body', mdbCardBody);
    Vue.component('mdb-card-footer', mdbCardFooter);
    Vue.component('mdb-card-header', mdbCardHeader);
    Vue.component('mdb-card-image', mdbCardImage);
    Vue.component('mdb-card-text', mdbCardText);
    Vue.component('mdb-card-title', mdbCardTitle);
    Vue.component('mdb-card-group', mdbCardGroup);
    Vue.component('mdb-carousel', mdbCarousel);
    Vue.component('mdb-carousel-item', mdbCarouselItem);
    Vue.component('mdb-carousel-caption', mdbCarouselCaption);
    Vue.component('mdb-carousel-indicator', mdbCarouselIndicator);
    Vue.component('mdb-carousel-indicators', mdbCarouselIndicators);
    Vue.component('mdb-carousel-control', mdbCarouselControl);
    Vue.component('mdb-carousel-inner', mdbCarouselInner);
    Vue.component('mdb-line-chart', mdbLineChart);
    Vue.component('mdb-radar-chart', mdbRadarChart);
    Vue.component('mdb-bar-chart', mdbBarChart);
    Vue.component('mdb-polar-chart', mdbPolarChart);
    Vue.component('mdb-pie-chart', mdbPieChart);
    Vue.component('mdb-doughnut-chart', mdbDoughnutChart);
    Vue.component('mdb-bubble-chart', mdbBubbleChart);
    Vue.component('mdb-scatter-chart', mdbScatterChart);
    Vue.component('mdb-horizontal-bar-chart', mdbHorizontalBarChart);
    Vue.component('mdb-collapse', mdbCollapse);
    Vue.component('mdb-col', mdbCol);
    Vue.component('mdb-container', mdbContainer);
    Vue.component('mdb-datatable', mdbDatatable);
    Vue.component('mdb-dropdown', mdbDropdown);
    Vue.component('mdb-dropdown-item', mdbDropdownItem);
    Vue.component('mdb-dropdown-menu', mdbDropdownMenu);
    Vue.component('mdb-dropdown-toggle', mdbDropdownToggle);
    Vue.component('mdb-edge-header', mdbEdgeHeader);
    Vue.component('mdb-icon', mdbIcon);
    Vue.component('mdb-footer', mdbFooter);
    Vue.component('mdb-google-map', mdbGoogleMap);
    Vue.component('mdb-jumbotron', mdbJumbotron);
    Vue.component('mdb-list-group', mdbListGroup);
    Vue.component('mdb-list-group-item', mdbListGroupItem);
    Vue.component('mdb-input', mdbInput);
    Vue.component('mdb-mask', mdbMask);
    Vue.component('mdb-textarea', mdbTextarea);
    Vue.component('mdb-media', mdbMedia);
    Vue.component('mdb-media-body', mdbMediaBody);
    Vue.component('mdb-media-image', mdbMediaImage);
    Vue.component('mdb-navbar', mdbNavbar);
    Vue.component('mdb-navbar-brand', mdbNavbarBrand);
    Vue.component('mdb-navbar-nav', mdbNavbarNav);
    Vue.component('mdb-navbar-toggler', mdbNavbarToggler);
    Vue.component('mdb-nav-item', mdbNavItem);
    Vue.component('mdb-page-item', mdbPageItem);
    Vue.component('mdb-page-nav', mdbPageNav);
    Vue.component('mdb-pagination', mdbPagination);
    Vue.component('mdb-popover', mdbPopover);
    Vue.component('mdb-progress', mdbProgress);
    Vue.component('mdb-row', mdbRow);
    Vue.component('mdb-tbl', mdbTbl);
    Vue.component('mdb-tbl-body', mdbTblBody);
    Vue.component('mdb-tbl-head', mdbTblHead);
    Vue.component('mdb-tooltip', mdbTooltip);
    Vue.component('mdb-view', mdbView);
    Vue.mixin(waves);
    Vue.mixin(mdbClassMixin);
    Vue.directive('animateOnScroll', animateOnScroll);
    Vue.component('mdb-form-in-line', mdbFormInline);
    Vue.component('mdb-modal', mdbModal);
    Vue.component('mdb-modal-header', mdbModalHeader);
    Vue.component('mdb-modal-title', mdbModalTitle);
    Vue.component('mdb-modal-body', mdbModalBody);
    Vue.component('mdb-modal-footer', mdbModalFooter);
    Vue.component('mdb-numeric-input', mdbNumericInput);
    Vue.component('mdb-tab', mdbTab);
    Vue.component('mdb-tabs', mdbTabs);
    Vue.component('mdb-tab-item', mdbTabItem);
    Vue.component('mdb-tab-content', mdbTabContent);
    Vue.component('mdb-tab-pane', mdbTabPane);
    Vue.component('mdb-scrollbar', mdbScrollbar);
    Vue.component('mdb-stretched-link', mdbStretchedLink);
    Vue.component('mdb-toast-notification', mdbToastNotification);
    Vue.component('mdb-masonry', mdbMasonry);
    Vue.component('mdb-masonry-item', mdbMasonryItem);

    Vue.component('accordion', mdbAccordion);
    Vue.component('accordion-pane', mdbAccordionPane);
    Vue.component('alert', mdbAlert);
    Vue.component('badge', mdbBadge);
    Vue.component('breadcrumb', mdbBreadcrumb);
    Vue.component('breadcrumb-item', mdbBreadcrumbItem);
    Vue.component('btn', mdbBtn);
    Vue.component('btn-group', mdbBtnGroup);
    Vue.component('btn-toolbar', mdbBtnToolbar);
    Vue.component('card', mdbCard);
    Vue.component('card-avatar', mdbCardAvatar);
    Vue.component('card-up', mdbCardUp);
    Vue.component('card-body', mdbCardBody);
    Vue.component('card-footer', mdbCardFooter);
    Vue.component('card-header', mdbCardHeader);
    Vue.component('card-img', mdbCardImage);
    Vue.component('card-text', mdbCardText);
    Vue.component('card-title', mdbCardTitle);
    Vue.component('card-group', mdbCardGroup);
    Vue.component('carousel', mdbCarousel);
    Vue.component('carousel-item', mdbCarouselItem);
    Vue.component('carousel-caption', mdbCarouselCaption);
    Vue.component('carousel-indicator', mdbCarouselIndicator);
    Vue.component('carousel-indicators', mdbCarouselIndicators);
    Vue.component('carousel-navigation', mdbCarouselControl);
    Vue.component('carousel-inner', mdbCarouselInner);
    Vue.component('line-chart', mdbLineChart);
    Vue.component('radar-chart', mdbRadarChart);
    Vue.component('bar-chart', mdbBarChart);
    Vue.component('polar-chart', mdbPolarChart);
    Vue.component('pie-chart', mdbPieChart);
    Vue.component('doughnut-chart', mdbDoughnutChart);
    Vue.component('bubble-chart', mdbBubbleChart);
    Vue.component('scatter-chart', mdbScatterChart);
    Vue.component('horizontal-bar-chart', mdbHorizontalBarChart);
    Vue.component('collapse', mdbCollapse);
    Vue.component('col', mdbCol);
    Vue.component('container', mdbContainer);
    Vue.component('datatable', mdbDatatable);
    Vue.component('dropdown', mdbDropdown);
    Vue.component('dropdown-item', mdbDropdownItem);
    Vue.component('dropdown-menu', mdbDropdownMenu);
    Vue.component('dropdown-toggle', mdbDropdownToggle);
    Vue.component('edge-header', mdbEdgeHeader);
    Vue.component('fa', mdbIcon);
    Vue.component('footer', mdbFooter);
    Vue.component('google-map', mdbGoogleMap);
    Vue.component('jumbotron', mdbJumbotron);
    Vue.component('list-group', mdbListGroup);
    Vue.component('list-group-item', mdbListGroupItem);
    Vue.component('input', mdbInput);
    Vue.component('mask', mdbMask);
    Vue.component('textarea', mdbTextarea);
    Vue.component('media', mdbMedia);
    Vue.component('media-body', mdbMediaBody);
    Vue.component('media-image', mdbMediaImage);
    Vue.component('navbar', mdbNavbar);
    Vue.component('navbar-brand', mdbNavbarBrand);
    Vue.component('navbar-nav', mdbNavbarNav);
    Vue.component('navbar-collapse', mdbNavbarToggler);
    Vue.component('nav-item', mdbNavItem);
    Vue.component('page-item', mdbPageItem);
    Vue.component('page-nav', mdbPageNav);
    Vue.component('pagination', mdbPagination);
    Vue.component('popover', mdbPopover);
    Vue.component('progress', mdbProgress);
    Vue.component('row', mdbRow);
    Vue.component('tbl', mdbTbl);
    Vue.component('tbl-body', mdbTblBody);
    Vue.component('tbl-head', mdbTblHead);
    Vue.component('tooltip', mdbTooltip);
    Vue.component('view-wrapper', mdbView);
    Vue.component('form-in-line', mdbFormInline);
    Vue.component('modal', mdbModal);
    Vue.component('modal-header', mdbModalHeader);
    Vue.component('modal-title', mdbModalTitle);
    Vue.component('modal-body', mdbModalBody);
    Vue.component('modal-footer', mdbModalFooter);
    Vue.component('numeric-input', mdbNumericInput);
    Vue.component('tab', mdbTab);
    Vue.component('tabs', mdbTabs);
    Vue.component('tab-item', mdbTabItem);
    Vue.component('tab-content', mdbTabContent);
    Vue.component('tab-pane', mdbTabPane);
    Vue.component('scrollbar', mdbScrollbar);
    Vue.component('stretched-link', mdbStretchedLink);
    Vue.component('toast-notification', mdbToastNotification);
    Vue.component('masonry', mdbMasonry);
    Vue.component('masonry-item', mdbMasonryItem);
  }
};

export default mdbvue;
