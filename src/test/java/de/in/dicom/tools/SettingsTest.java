package de.in.dicom.tools;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.awt.Rectangle;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.junit.Before;
import org.junit.Test;

public class SettingsTest {

    @Before
    public void setUp() throws BackingStoreException {
        Preferences prefs = Preferences.userNodeForPackage(DicomEditor.class);
        prefs.clear();
    }

    @Test
    public void testFontSizePersistence() {
        assertEquals(12, Settings.getFontSize());
        Settings.setFontSize(20);
        assertEquals(20, Settings.getFontSize());

        // Test validation
        Settings.setFontSize(100); // Too large
        assertEquals(20, Settings.getFontSize());

        Settings.setFontSize(2); // Too small
        assertEquals(20, Settings.getFontSize());
    }

    @Test
    public void testLastPathPersistence() {
        assertNull(Settings.getLastPath());
        Settings.setLastPath("/tmp/test");
        assertEquals("/tmp/test", Settings.getLastPath());
    }

    @Test
    public void testWindowBoundsPersistence() {
        // Initially null
        assertNull(Settings.getWindowBounds());

        Rectangle bounds = new Rectangle(10, 10, 800, 600);
        Settings.setWindowBounds(bounds);

        // Verify directly in preferences since getWindowBounds() might fail
        // isValidBounds() in headless
        Preferences prefs = Preferences.userNodeForPackage(DicomEditor.class);
        assertEquals(10, prefs.getInt("window.x", -1));
        assertEquals(10, prefs.getInt("window.y", -1));
        assertEquals(800, prefs.getInt("window.width", -1));
        assertEquals(600, prefs.getInt("window.height", -1));
    }
}
