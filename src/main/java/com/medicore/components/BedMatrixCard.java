package com.medicore.components;

import com.medicore.model.Bed;
import com.medicore.theme.MediCoreTheme;
import com.medicore.theme.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Visual Bed Occupancy card with telemetry, EVS timer, and acuity indicator.
 */
public class BedMatrixCard extends JPanel {
    private final Bed bed;
    private final Runnable onClickAction;

    public BedMatrixCard(Bed bed, Runnable onClickAction) {
        this.bed = bed;
        this.onClickAction = onClickAction;
        setLayout(new BorderLayout(4, 4));
        setOpaque(false);
        setBorder(new EmptyBorder(8, 10, 8, 10));
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        initUI();

        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (onClickAction != null) onClickAction.run();
            }
        });
    }

    private void initUI() {
        // Top row: Bed Number and Status badge
        JPanel topRow = new JPanel(new BorderLayout(6, 0));
        topRow.setOpaque(false);

        JLabel bedNumLbl = new JLabel(bed.getBedId());
        bedNumLbl.setFont(MediCoreTheme.FONT_SUBHEADER);
        bedNumLbl.setForeground(MediCoreTheme.TEXT_PRIMARY);
        topRow.add(bedNumLbl, BorderLayout.WEST);

        JLabel statusBadge;
        switch (bed.getStatus()) {
            case OCCUPIED:
                statusBadge = UIUtils.createAcuityBadge(bed.getAcuityLevel());
                break;
            case AVAILABLE:
                statusBadge = UIUtils.createPillBadge("CLEAN / READY", MediCoreTheme.ESI4_TEXT, MediCoreTheme.ESI4_BG, MediCoreTheme.ESI4_BORDER);
                break;
            case DISINFECTION:
                statusBadge = UIUtils.createPillBadge("EVS (" + bed.getEvsTimerRemainingMinutes() + "m)", MediCoreTheme.ESI3_TEXT, MediCoreTheme.ESI3_BG, MediCoreTheme.ESI3_BORDER);
                break;
            case DIRTY:
                statusBadge = UIUtils.createPillBadge("DIRTY - EVS", MediCoreTheme.ESI1_TEXT, MediCoreTheme.ESI1_BG, MediCoreTheme.ESI1_BORDER);
                break;
            default:
                statusBadge = UIUtils.createPillBadge("HOLD", MediCoreTheme.TEXT_SECONDARY, MediCoreTheme.BG_PANEL, MediCoreTheme.BORDER_DARK);
                break;
        }
        topRow.add(statusBadge, BorderLayout.EAST);
        add(topRow, BorderLayout.NORTH);

        // Center: Patient info or status details
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);

        if (bed.getStatus() == Bed.Status.OCCUPIED) {
            JLabel ptName = new JLabel(bed.getPatientName() != null ? bed.getPatientName() : "Unknown");
            ptName.setFont(MediCoreTheme.FONT_BODY_BOLD);
            ptName.setForeground(MediCoreTheme.TEXT_PRIMARY);

            JLabel mrnLbl = new JLabel(bed.getPatientMrn());
            mrnLbl.setFont(MediCoreTheme.FONT_SMALL);
            mrnLbl.setForeground(MediCoreTheme.TEXT_MUTED);

            JLabel vitalsLbl = new JLabel(bed.getTelemetryVitals() != null ? bed.getTelemetryVitals() : "Telemetry Active");
            vitalsLbl.setFont(MediCoreTheme.FONT_MONO);
            if (bed.getTelemetryVitals() != null && bed.getTelemetryVitals().contains("!")) {
                vitalsLbl.setForeground(MediCoreTheme.ESI1_TEXT);
                vitalsLbl.setFont(MediCoreTheme.FONT_MONO_BOLD);
            } else {
                vitalsLbl.setForeground(MediCoreTheme.ESI4_TEXT);
            }

            centerPanel.add(Box.createVerticalStrut(2));
            centerPanel.add(ptName);
            centerPanel.add(mrnLbl);
            centerPanel.add(Box.createVerticalStrut(4));
            centerPanel.add(vitalsLbl);
        } else if (bed.getStatus() == Bed.Status.DISINFECTION) {
            JLabel evsLbl = new JLabel("Terminal UV-C Disinfection");
            evsLbl.setFont(MediCoreTheme.FONT_SMALL);
            evsLbl.setForeground(MediCoreTheme.ESI3_TEXT);

            JLabel timerLbl = new JLabel("Reopening in ~" + bed.getEvsTimerRemainingMinutes() + " mins");
            timerLbl.setFont(MediCoreTheme.FONT_BODY_BOLD);
            timerLbl.setForeground(MediCoreTheme.TEXT_PRIMARY);

            centerPanel.add(Box.createVerticalStrut(4));
            centerPanel.add(evsLbl);
            centerPanel.add(timerLbl);
        } else if (bed.getStatus() == Bed.Status.DIRTY) {
            JLabel alertLbl = new JLabel("Sanitation Request Dispatched");
            alertLbl.setFont(MediCoreTheme.FONT_SMALL);
            alertLbl.setForeground(MediCoreTheme.ESI1_TEXT);

            JLabel subLbl = new JLabel("Priority: STAT Turnaround");
            subLbl.setFont(MediCoreTheme.FONT_BODY);
            subLbl.setForeground(MediCoreTheme.TEXT_SECONDARY);

            centerPanel.add(Box.createVerticalStrut(4));
            centerPanel.add(alertLbl);
            centerPanel.add(subLbl);
        } else {
            JLabel readyLbl = new JLabel("Bed Cleaned & Disinfected");
            readyLbl.setFont(MediCoreTheme.FONT_SMALL);
            readyLbl.setForeground(MediCoreTheme.ESI4_TEXT);

            JLabel subLbl = new JLabel("HL7 Inpatient ADT Ready");
            subLbl.setFont(MediCoreTheme.FONT_BODY);
            subLbl.setForeground(MediCoreTheme.TEXT_SECONDARY);

            centerPanel.add(Box.createVerticalStrut(4));
            centerPanel.add(readyLbl);
            centerPanel.add(subLbl);
        }

        add(centerPanel, BorderLayout.CENTER);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color cardBg = MediCoreTheme.BG_WHITE;
        Color borderColor = MediCoreTheme.BORDER_LIGHT;

        if (bed.getStatus() == Bed.Status.OCCUPIED) {
            if (bed.getAcuityLevel() == 1) {
                borderColor = MediCoreTheme.ESI1_BORDER;
            } else if (bed.getAcuityLevel() == 2) {
                borderColor = MediCoreTheme.ESI2_BORDER;
            }
        } else if (bed.getStatus() == Bed.Status.DIRTY) {
            borderColor = MediCoreTheme.ESI1_BORDER;
            cardBg = MediCoreTheme.ESI1_BG;
        }

        g2.setColor(cardBg);
        g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
        g2.setColor(borderColor);
        g2.setStroke(new BasicStroke(1.2f));
        g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 8, 8));
        g2.dispose();
        super.paintComponent(g);
    }
}
