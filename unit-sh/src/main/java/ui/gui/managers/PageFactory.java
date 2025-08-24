// File: ui/gui/managers/PageFactory.java
package ui.gui.managers;

import devices.DeviceType;
import ui.gui.devicesListPages.ChooseLightsUpdatePage;
import ui.gui.devicesListPages.ChooseUtilDevicePage;

import javax.swing.*;

public class PageFactory {

    public static JComponent loadPage(int pageIndex, int basePageId, DeviceType[] types) {
        if (isLightMatrix(types)) {
            return ChooseLightsUpdatePage.loadFresh(pageIndex, basePageId, types);
        } else {
            return ChooseUtilDevicePage.loadFresh(pageIndex, basePageId, types);
        }
    }

    private static boolean isLightMatrix(DeviceType[] types) {
        for (DeviceType type : types) {
            if (type == DeviceType.LIGHT || type == DeviceType.SMART_LIGHT) {
                return true;
            }
        }
        return false;
    }
}
