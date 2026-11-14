package com.medicore.views;

import com.medicore.dialogs.LoginDialog;
import com.medicore.model.HospitalDataStore;
import com.medicore.model.StaffUser;
import com.medicore.theme.MediCoreTheme;
import com.medicore.theme.UIUtils;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Master Clinical Desktop Client Window Chrome with Menus, Live Header, Nav Sidebar, and Card Content.
 */
public class MainFrame extends JFrame implements DashboardView.ViewNavigator {
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(cardLayout);

    private final DashboardView dashboardView;
    private final PatientRegistrationView registrationView;
    private final PatientDirectoryView directoryView;
    private final UpdateRecordsView updateRecordsView;
    private final BedShiftManagementView bedShiftView;
    private final PharmacyEmarView pharmacyView;
    private final DiagnosticsView diagnosticsView;

    private final JLabel clockLabel = new JLabel();
    private final JLabel staffBadgeLabel = new JLabel();
    private final Map<String, JButton> navButtons = new HashMap<>();
    private String currentActiveView = "Dashboard";

    public MainFrame() {
        super("MediCore HMS v3.4 – Clinical Desktop Client");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1440, 900);
        setMinimumSize(new Dimension(1200, 750));
        setLocationRelativeTo(null);

        // Build views
        dashboardView = new DashboardView(this);
        registrationView = new PatientRegistrationView(this);
        directoryView = new PatientDirectoryView(this);
        updateRecordsView = new UpdateRecordsView(this);
        bedShiftView = new BedShiftManagementView(this);
        pharmacyView = new PharmacyEmarView(this);
        diagnosticsView = new DiagnosticsView();

        contentPanel.add(dashboardView, "Dashboard");
        contentPanel.add(registrationView, "Register Patient");
        contentPanel.add(directoryView, "Patient Search");
        contentPanel.add(updateRecordsView, "Update Records");
        contentPanel.add(bedShiftView, "Ward & Bed Changes");
        contentPanel.add(pharmacyView, "eMAR & Pharmacy");
        contentPanel.add(diagnosticsView, "Diagnostics");

        // Menu Bar
        setJMenuBar(createMenuBar());

        // Root container: Top Live Header + Center (Left Sidebar + Content)
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(MediCoreTheme.BG_STERILE);

        root.add(createLiveHeader(), BorderLayout.NORTH);

        JPanel mainBody = new JPanel(new BorderLayout());
        mainBody.setOpaque(false);
        mainBody.add(createNavigationSidebar(), BorderLayout.WEST);
        mainBody.add(contentPanel, BorderLayout.CENTER);

        root.add(mainBody, BorderLayout.CENTER);
        setContentPane(root);

        // Hospital Synchronized Clock Timer (1 second interval)
        Timer clockTimer = new Timer(1000, e -> updateClock());
        clockTimer.start();
        updateClock();

        updateStaffDisplay();
    }
    private JMenuBar createMenuBar() {
        JMenuBar mb = new JMenuBar();
        mb.setBackground(MediCoreTheme.BG_WHITE);

        JMenu fileMenu = new JMenu("File");
        JMenuItem switchUser = new JMenuItem("Switch Clinician Account / Re-login...");
        switchUser.addActionListener(e -> promptRelogin());
        JMenuItem exitItem = new JMenuItem("Exit Clinical Client");
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(switchUser);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        JMenu ptMenu = new JMenu("Patients");
        JMenuItem regPt = new JMenuItem("Register New Inpatient (ADT-A01)");
        regPt.addActionListener(e -> navigateTo("Register Patient", null));
        JMenuItem searchPt = new JMenuItem("Universal Patient Directory Search");
        searchPt.addActionListener(e -> navigateTo("Patient Search", null));
        ptMenu.add(regPt);
        ptMenu.add(searchPt);

        JMenu admMenu = new JMenu("Admissions");
        JMenuItem bedTrans = new JMenuItem("Inpatient Bed Transfer (ADT-A02)");
        bedTrans.addActionListener(e -> navigateTo("Ward & Bed Changes", null));
        JMenuItem shiftHand = new JMenuItem("Charge Nurse Shift Handoff (SBAR)");
        shiftHand.addActionListener(e -> navigateTo("Ward & Bed Changes", null));
        admMenu.add(bedTrans);
        admMenu.add(shiftHand);

        JMenu recMenu = new JMenu("Records");
        JMenuItem bedChart = new JMenuItem("Bedside Chart & Progress Notes");
        bedChart.addActionListener(e -> navigateTo("Update Records", null));
        recMenu.add(bedChart);

        JMenu pharmMenu = new JMenu("Pharmacy");
        JMenuItem emarItem = new JMenuItem("eMAR Administration & Dispensing");
        emarItem.addActionListener(e -> navigateTo("eMAR & Pharmacy", null));
        pharmMenu.add(emarItem);

        JMenu toolsMenu = new JMenu("Tools");
        JMenuItem diagItem = new JMenuItem("JVM & System Diagnostics...");
        diagItem.addActionListener(e -> navigateTo("Diagnostics", null));
        toolsMenu.add(diagItem);

        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutItem = new JMenuItem("About MediCore HMS v3.4");
        aboutItem.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "MediCore HMS v3.4 — Clinical Desktop Client\n" +
                        "HL7 v2.5 / FHIR R4 Compliant Clinical Information System\n" +
                        "Built with Java Swing & FlatLaf Theme Engine\n" +
                        "© 2026 MediCore Health Systems Inc. All Rights Reserved.",
                "About MediCore HMS", JOptionPane.INFORMATION_MESSAGE));
        helpMenu.add(aboutItem);

        mb.add(fileMenu);
        mb.add(ptMenu);
        mb.add(admMenu);
        mb.add(recMenu);
        mb.add(pharmMenu);
        mb.add(toolsMenu);
        mb.add(helpMenu);
        return mb;
    }

    private JPanel createLiveHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(MediCoreTheme.BG_WHITE);
        header.setBorder(new CompoundBorder(new LineBorder(MediCoreTheme.BORDER_LIGHT, 1), new EmptyBorder(8, 16, 8, 16)));

        // Left brand & link status
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);

        JLabel logo = new JLabel("✚ MEDICORE HMS");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 15));
        logo.setForeground(MediCoreTheme.PRIMARY_ACCENT);

        JLabel linkBadge = UIUtils.createPillBadge("● HL7/FHIR LINK: 99.98% OK", MediCoreTheme.ESI4_TEXT, MediCoreTheme.ESI4_BG, MediCoreTheme.ESI4_BORDER);

        left.add(logo);
        left.add(linkBadge);
        header.add(left, BorderLayout.WEST);

        // Right user badge & synchronized hospital clock
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        right.setOpaque(false);

        staffBadgeLabel.setFont(MediCoreTheme.FONT_BODY_BOLD);
        staffBadgeLabel.setForeground(MediCoreTheme.TEXT_PRIMARY);

        clockLabel.setFont(MediCoreTheme.FONT_MONO_BOLD);
        clockLabel.setForeground(MediCoreTheme.PRIMARY_DARK);
        clockLabel.setBorder(new CompoundBorder(new LineBorder(MediCoreTheme.BORDER_LIGHT, 1, true), new EmptyBorder(4, 8, 4, 8)));

        JButton switchBtn = UIUtils.createSecondaryButton("Switch");
        switchBtn.addActionListener(e -> promptRelogin());

        right.add(staffBadgeLabel);
        right.add(switchBtn);
        right.add(clockLabel);
        header.add(right, BorderLayout.EAST);

        return header;
    }

    private JPanel createNavigationSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(MediCoreTheme.BG_WHITE);
        sidebar.setBorder(new CompoundBorder(new LineBorder(MediCoreTheme.BORDER_LIGHT, 1), new EmptyBorder(14, 12, 14, 12)));
        sidebar.setPreferredSize(new Dimension(230, 0));

        // Clinical Operations section
        JLabel clinLbl = new JLabel("CLINICAL OPERATIONS");
        clinLbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        clinLbl.setForeground(MediCoreTheme.TEXT_MUTED);
        clinLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(clinLbl);
        sidebar.add(Box.createVerticalStrut(6));

        addNavButton(sidebar, "Dashboard", "📊 Dashboard");
        addNavButton(sidebar, "Register Patient", "📝 Register Patient");
        addNavButton(sidebar, "Patient Search", "🔍 Patient Search");
        addNavButton(sidebar, "Update Records", "📋 Update Records");
        addNavButton(sidebar, "Ward & Bed Changes", "🛏 Ward & Bed Changes");
        addNavButton(sidebar, "eMAR & Pharmacy", "💊 eMAR & Pharmacy");

        sidebar.add(Box.createVerticalStrut(16));

        // System Settings section
        JLabel sysLbl = new JLabel("SYSTEM SETTINGS");
        sysLbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        sysLbl.setForeground(MediCoreTheme.TEXT_MUTED);
        sysLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(sysLbl);
        sidebar.add(Box.createVerticalStrut(6));

        addNavButton(sidebar, "Diagnostics", "⚙ JVM & Diagnostics");

        sidebar.add(Box.createVerticalGlue());

        // Footer version & node badge
        JPanel foot = new JPanel(new GridLayout(2, 1, 0, 2));
        foot.setOpaque(false);
        foot.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel vLbl = new JLabel("MediCore Client v3.4.0");
        vLbl.setFont(MediCoreTheme.FONT_SMALL);
        vLbl.setForeground(MediCoreTheme.TEXT_MUTED);

        JLabel nodeLbl = new JLabel("Terminal: PHX-WS-09A");
        nodeLbl.setFont(MediCoreTheme.FONT_SMALL);
        nodeLbl.setForeground(MediCoreTheme.TEXT_MUTED);

        foot.add(vLbl);
        foot.add(nodeLbl);
        sidebar.add(foot);

        updateNavButtonStyles();
        return sidebar;
    }

    private void addNavButton(JPanel container, String viewKey, String label) {
        JButton btn = new JButton(label);
        btn.setFont(MediCoreTheme.FONT_BODY_BOLD);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setMaximumSize(new Dimension(Short.MAX_VALUE, 38));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);

        btn.addActionListener(e -> navigateTo(viewKey, null));

        navButtons.put(viewKey, btn);
        container.add(btn);
        container.add(Box.createVerticalStrut(4));
    }

    private void updateNavButtonStyles() {
        for (Map.Entry<String, JButton> entry : navButtons.entrySet()) {
            JButton btn = entry.getValue();
            boolean active = entry.getKey().equals(currentActiveView);
            if (active) {
                btn.setBackground(MediCoreTheme.PRIMARY_ACCENT);
                btn.setForeground(Color.WHITE);
                btn.setBorder(new CompoundBorder(new LineBorder(MediCoreTheme.PRIMARY_DARK, 1, true), new EmptyBorder(6, 12, 6, 12)));
            } else {
                btn.setBackground(MediCoreTheme.BG_WHITE);
                btn.setForeground(MediCoreTheme.TEXT_PRIMARY);
                btn.setBorder(new CompoundBorder(new LineBorder(MediCoreTheme.BG_WHITE, 1, true), new EmptyBorder(6, 12, 6, 12)));
            }
        }
    }

    @Override
    public void navigateTo(String viewName, String patientMrn) {
        this.currentActiveView = viewName;
        cardLayout.show(contentPanel, viewName);
        updateNavButtonStyles();

        if (patientMrn != null) {
            if ("Update Records".equals(viewName)) {
                updateRecordsView.selectPatient(patientMrn);
            }
        }
    }

    private void updateClock() {
        LocalTime now = LocalTime.now();
        clockLabel.setText("SYNC: " + now.format(DateTimeFormatter.ofPattern("HH:mm:ss")) + " EST");
    }

    private void updateStaffDisplay() {
        StaffUser user = HospitalDataStore.getInstance().getCurrentStaff();
        if (user != null) {
            staffBadgeLabel.setText("👤 " + user.getBadgeDisplay());
        }
    }

    private void promptRelogin() {
        LoginDialog dlg = new LoginDialog(this);
        dlg.setVisible(true);
        if (dlg.isAuthenticated()) {
            updateStaffDisplay();
        }
    }
}
