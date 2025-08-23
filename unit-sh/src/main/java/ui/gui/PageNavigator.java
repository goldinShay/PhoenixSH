package ui.gui;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * A centralized utility for registering and navigating GUI pages using CardLayout.
 */
public class PageNavigator {

    private static JPanel container;                // Root panel containing all views
    private static CardLayout cardLayout;           // Layout manager for switching views

    private static final Map<Integer, String> pageMap = new HashMap<>();      // Page number → card name
    private static Map<Integer, JComponent> pageRegistry = new HashMap<>();

    private static int currentPageId = -1;

    /**
     * Initializes the navigator with a root panel using CardLayout.
     */
    public static void initialize(JPanel rootPanel) {
        if (rootPanel.getLayout() instanceof CardLayout layoutInstance) {
            container = rootPanel;
            cardLayout = layoutInstance;
        } else {
            throw new IllegalArgumentException("PageNavigator requires a JPanel with CardLayout.");
        }
    }

    /**
     * Registers a new page with a specific ID. Ignore duplicates.
     */
    public static void registerPage(int pageNumber, JComponent page) {
        String pageKey = "PAGE_" + pageNumber;

        if (container == null) {
            throw new IllegalStateException("PageNavigator must be initialized before registering pages.");
        }

        if (pageMap.containsKey(pageNumber)) {
            // ✅ Replace existing page
            container.remove(pageRegistry.get(pageNumber));
            container.add(page, pageKey);
            pageRegistry.put(pageNumber, page);
            System.out.println("🔁 Replaced page " + pageNumber + " with " + page.getComponentCount() + " components.");
        } else {
            // ✅ First-time registration
            container.add(page, pageKey);
            pageMap.put(pageNumber, pageKey);
            pageRegistry.put(pageNumber, page);
            System.out.println("📄 Registered page " + pageNumber + " with " + page.getComponentCount() + " components.");
        }

        container.revalidate();
        container.repaint();
    }


    /**
     * Switches to a page by its numeric ID.
     */
    public static void goToPage(int pageNumber) {
        String pageKey = pageMap.get(pageNumber);
        if (pageKey != null && container != null && cardLayout != null) {
            cardLayout.show(container, pageKey);
            container.revalidate(); // ✅ Ensures layout updates
            container.repaint();    // ✅ Forces visual refresh
            currentPageId = pageNumber;
            System.out.println("🔀 Switched to page " + pageNumber);
        } else {
            System.err.printf("❌ Page %d not found or PageNavigator not initialized.%n", pageNumber);
        }
    }

    /**
     * Registers and switches to a page in one step (useful for dynamic paging).
     */
    public static void loadAndGoTo(int pageNumber, JPanel page) {
        registerPage(pageNumber, page);
        goToPage(pageNumber);
    }

    /**
     * Checks if a page is already registered.
     */
    public static boolean isPageRegistered(int pageNumber) {
        return pageMap.containsKey(pageNumber);
    }

    /**
     * Returns the number of registered pages.
     */
    public static int getRegisteredCount() {
        return pageMap.size();
    }

    /**
     * Clears all pages and resets state.
     */
    public static void clearPages() {
        pageMap.clear();
        pageRegistry.clear();
        if (container != null) {
            container.removeAll();
            container.revalidate();
            container.repaint();
        }
        System.out.println("🧹 PageNavigator pages cleared.");
    }

    /**
     * Returns the ID of the currently displayed page.
     */
    public static int getCurrentPageId() {
        return currentPageId;
    }

    /**
     * Returns the actual JPanel for a given page ID.
     */
    public static JComponent getPage(int pageNumber) {
        return pageRegistry.get(pageNumber);
    }
}
