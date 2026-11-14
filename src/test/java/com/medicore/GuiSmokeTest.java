package com.medicore;

import com.medicore.dialogs.LoginDialog;
import com.medicore.theme.MediCoreTheme;
import com.medicore.views.MainFrame;
import org.junit.Assert;
import org.junit.Test;

import javax.swing.*;

/**
 * Automated smoke test verifying GUI views and navigation cycle without exceptions.
 */
public class GuiSmokeTest {

    @Test
    public void testGuiInitializationAndNavigation() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            try {
                // 1. Setup Theme
                MediCoreTheme.setup();

                // 2. Initialize Login Dialog
                LoginDialog loginDialog = new LoginDialog(null);
                Assert.assertNotNull(loginDialog);
                loginDialog.dispose();

                // 3. Initialize MainFrame
                MainFrame frame = new MainFrame();
                Assert.assertNotNull(frame);

                // 4. Test Navigation to each screen
                frame.navigateTo("Dashboard", null);
                frame.navigateTo("Register Patient", null);
                frame.navigateTo("Patient Search", null);
                frame.navigateTo("Update Records", "MRN-84920");
                frame.navigateTo("Ward & Bed Changes", "MRN-71932");
                frame.navigateTo("eMAR & Pharmacy", null);
                frame.navigateTo("Diagnostics", null);

                // Cycle back to Dashboard
                frame.navigateTo("Dashboard", null);

                frame.dispose();
            } catch (Exception ex) {
                Assert.fail("GUI smoke test encountered exception: " + ex.getMessage());
            }
        });
    }
}