package launcher;

import launcher.managers.SessionManager;
import launcher.objects.EpicCategory;
import launcher.objects.EpicItem;
import launcher.objects.HtmlUtils;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

public class AssetListFormOwned extends AssetListForm {

    public AssetListFormOwned(){
        super();

        _AssetListForm.setBorder(new TitledBorder("Owned Assets"));

        _scrollPane.getVerticalScrollBar().addAdjustmentListener(event -> {
            if (_viewingItem || isReloading)
                return;

            JScrollBar scrollBar = (JScrollBar) event.getAdjustable();
            int extent = scrollBar.getModel().getExtent();
            if (scrollBar.getMaximum() - (scrollBar.getValue() + extent) > 6)
                return;
            if (_itemsPerPage >= SessionManager.getInstance().getUser().getOwnedItems().size())
                return;
            reloadList();
        });




    }

    private boolean shouldDisplay(EpicItem item){
        boolean downloaded = item.getLastDownloadTime(SessionManager.getInstance().getUser().getCurrentProject()) != -1;

        switch(ownedAssetsFilter){
            case DOWNLOADED:
                if(!downloaded)
                    return false;
                break;
            case NOTDOWNLOADED:
                if(downloaded)
                    return false;
                break;
        }

        if(filterByVendor.length() > 0 && !item.getSellerName().equals(filterByVendor))
            return false;

        if(filterByCategory.length() > 0) {
            boolean found = false;
            for(EpicCategory cat : item.getCategories())
                if(cat.getName().equals(filterByCategory))
                    found = true;

            if(!found)
                return false;
        }

        if(filterByText.length() > 0 && !item.getName().contains(filterByText) && !item.getSellerName().contains(filterByText) && !item.getDescription().contains(filterByText) && !item.getLongDescription().contains(filterByText))
            return false;

        return true;
    }

    public void reloadList() {
        HashSet<String> vendors = new HashSet<>();
        HashSet<String> categories = new HashSet<>();

        ArrayList<String> tableElements = new ArrayList<>();

        int i = 0;
        if (SessionManager.getInstance().getUser().getOwnedItems() != null) {
            for (EpicItem item : SessionManager.getInstance().getUser().getOwnedItems()) {
                if(!shouldDisplay(item))
                    continue;

                if (i >= _itemsPerPage)
                    break;

                String asset = HtmlUtils.getAssetDiv();
                asset = asset.replaceAll("%category%", item.getCategories().size() == 0 ? "Unknown" : item.getCategories().get(0).getName());
                String name = HtmlUtils.findText(item.getName(), 195, HtmlUtils.FONT_TITLE);
                asset = asset.replaceAll("%title%", name);
                asset = asset.replaceAll("%image%", item.getThumbnail().getUrl());
                asset = asset.replaceAll("%owned%", HtmlUtils.getAssetDivOwner());
                asset = asset.replaceAll("%price%", item.isCompatible(SessionManager.getInstance().getUser().getUnrealEngineVersion()) ? "" : "Not compatible");

                asset = parseData(new ArrayList<>(Arrays.asList("%lastDownload%", "%creator%", "%id%")), asset, item);

                tableElements.add(asset);

                i++;

                vendors.add(item.getSellerName());
                for(EpicCategory cat : item.getCategories())
                    categories.add(cat.getName());
            }
        }

        // build html data
        String body = "<table class=\"asset-container\">";

        i=0;
        for(String element : tableElements){
            if(i % itemsInLine == 0)
                body+="<tr>";

            body+="<td>" + element + "</td>";

            i++;
            if(i % itemsInLine == 0)
                body+="<tr>";
        }

        body+="</table>";

        _textPane1.setText(HtmlUtils.toHTML(HtmlUtils.getMarketHead(), body));

        if (_itemsPerPage == 8) {
            _textPane1.setCaretPosition(0);
            _scrollPane.getVerticalScrollBar().setValue(0);
        }

        setVendors(vendors);
        setCategories(categories);
    }
}
