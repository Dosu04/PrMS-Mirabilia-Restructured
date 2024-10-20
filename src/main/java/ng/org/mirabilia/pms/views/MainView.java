package ng.org.mirabilia.pms.views;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.spring.security.AuthenticationContext;
import ng.org.mirabilia.pms.entities.User;
import ng.org.mirabilia.pms.services.UserService;
import ng.org.mirabilia.pms.views.components.NavItem;
import ng.org.mirabilia.pms.views.forms.settings.EditProfileForm;
import ng.org.mirabilia.pms.views.modules.dashboard.DashboardView;
import ng.org.mirabilia.pms.views.modules.finances.FinancesView;
import ng.org.mirabilia.pms.views.modules.location.LocationView;
import ng.org.mirabilia.pms.views.modules.logs.LogsView;
import ng.org.mirabilia.pms.views.modules.properties.PropertiesView;
import ng.org.mirabilia.pms.views.modules.support.SupportView;
import ng.org.mirabilia.pms.views.modules.users.UsersView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.List;

public class MainView extends AppLayout implements AfterNavigationObserver {

    private final List<RouterLink> routerLinks = new ArrayList<>();

    @Autowired
    private AuthenticationContext authContext;

    @Autowired
    private UserService userService;

    public MainView(AuthenticationContext authContext, UserService userService) {
        this.authContext = authContext;
        this.userService = userService;
        configureHeader();
        configureDrawer();
        configureMainContent();
    }

    private void configureHeader() {
        DrawerToggle toggle = new DrawerToggle();
        toggle.addClassName("custom-toggle-button");

        Icon settingsIcon = VaadinIcon.COG.create();
        Button settingsButton = new Button(settingsIcon, e -> openEditProfileDialog());
        settingsButton.addClassName("custom-settings-button");

        HorizontalLayout header = new HorizontalLayout(toggle, settingsButton);
        header.addClassName("custom-header");
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.setWidthFull();


        addToNavbar(header);
        setPrimarySection(Section.DRAWER);
    }

    private void configureDrawer() {
        Image logo = new Image("images/logo.png", "Logo");
        logo.addClassName("drawer-logo");

        VerticalLayout drawerContent = new VerticalLayout(logo);

        if (hasRole("ROLE_ADMIN") || hasRole("ROLE_MANAGER") || hasRole("ROLE_AGENT") || hasRole("ROLE_ACCOUNTANT") ||
                hasRole("ROLE_CRO") || hasRole("ROLE_CLIENT") || hasRole("ROLE_IT_SUPPORT")) {
            RouterLink dashboardLink = createNavItem("Dashboard", VaadinIcon.DASHBOARD, DashboardView.class);
            drawerContent.add(dashboardLink);
        }

        if (hasRole("ROLE_ADMIN") || hasRole("ROLE_MANAGER")) {
            RouterLink locationLink = createNavItem("Location", VaadinIcon.LOCATION_ARROW, LocationView.class);
            drawerContent.add(locationLink);
        }

        if (hasRole("ROLE_ADMIN") || hasRole("ROLE_MANAGER") || hasRole("ROLE_IT_SUPPORT")) {
            RouterLink usersLink = createNavItem("Users", VaadinIcon.USERS, UsersView.class);
            drawerContent.add(usersLink);
        }

        if (hasRole("ROLE_ADMIN") || hasRole("ROLE_MANAGER") || hasRole("ROLE_AGENT") || hasRole("ROLE_CLIENT")) {
            RouterLink propertiesLink = createNavItem("Properties", VaadinIcon.WORKPLACE, PropertiesView.class);
            drawerContent.add(propertiesLink);
        }

        if (hasRole("ROLE_ADMIN") || hasRole("ROLE_MANAGER") || hasRole("ROLE_ACCOUNTANT") || hasRole("ROLE_CLIENT")) {
            RouterLink financesLink = createNavItem("Finances", VaadinIcon.BAR_CHART, FinancesView.class);
            drawerContent.add(financesLink);
        }

        if (hasRole("ROLE_ADMIN") || hasRole("ROLE_MANAGER") || hasRole("ROLE_AGENT") || hasRole("ROLE_ACCOUNTANT") ||
                hasRole("ROLE_CRO") || hasRole("ROLE_CLIENT") || hasRole("ROLE_IT_SUPPORT")) {
            RouterLink supportLink = createNavItem("Support", VaadinIcon.HEADSET, SupportView.class);
            drawerContent.add(supportLink);
        }

        if (hasRole("ROLE_ADMIN") || hasRole("ROLE_MANAGER") || hasRole("ROLE_IT_SUPPORT")) {
            RouterLink logsLink = createNavItem("Logs", VaadinIcon.CLIPBOARD_TEXT, LogsView.class);
            drawerContent.add(logsLink);
        }

        drawerContent.addClassName("drawer-content");

        Button logoutButton = new Button("Logout", VaadinIcon.SIGN_OUT.create(), event -> authContext.logout());
        logoutButton.addClassName("custom-logout-button");
        logoutButton.addClassName("drawer-link");

        drawerContent.add(logoutButton);

        addToDrawer(drawerContent);
    }

    private void configureMainContent() {
        VerticalLayout content = new VerticalLayout();
        content.addClassName("main-content");
        setContent(content);
    }

    private RouterLink createNavItem(String label, VaadinIcon icon, Class<? extends com.vaadin.flow.component.Component> navigationTarget) {
        RouterLink link = new RouterLink();
        link.addClassName("drawer-link");

        link.add(new NavItem(icon.create(), label));
        link.setRoute(navigationTarget);

        routerLinks.add(link);

        return link;
    }

    private boolean hasRole(String role) {
        return authContext.getAuthenticatedUser(UserDetails.class)
                .map(authUser -> authUser.getAuthorities().stream()
                        .anyMatch(authority -> authority.getAuthority().equals(role)))
                .orElse(false);
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        String activeUrl = event.getLocation().getPath();

        routerLinks.forEach(link -> {
            if (link.getHref().equals(activeUrl)) {
                link.addClassName("active-link");
            } else {
                link.removeClassName("active-link");
            }
        });
    }

    private void openEditProfileDialog() {
        authContext.getAuthenticatedUser(UserDetails.class).ifPresent(userDetails -> {
            User loggedInUser = userService.findByUsername(userDetails.getUsername());
            if (loggedInUser != null) {
                EditProfileForm editProfileForm = new EditProfileForm(userService, loggedInUser, updated -> {
                });
                editProfileForm.open();
            }
        });
    }




}
