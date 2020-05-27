package launcher;


import launcher.objects.HtmlUtils;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Arrays;

public class AssetListFormMarketplace extends AssetListForm {

    public AssetListFormMarketplace(){
        super();

        _scrollPane.getVerticalScrollBar().addAdjustmentListener(event -> {
            if (_viewingItem || _currentCategory == null)
                return;
            JScrollBar scrollBar = (JScrollBar) event.getAdjustable();
            int extent = scrollBar.getModel().getExtent();

            if (scrollBar.getMaximum() - (scrollBar.getValue() + extent) > 6)
                return;

            long size = _currentCategory.getItems().values().stream().filter(item -> item.getPrice() <= _filter.getPrice()).count();
            if (_itemsPerPage >= size)
                return;

            _itemsPerPage += 8;
            reloadList();
        });
    }

    public void reloadList(){
        String html = HtmlUtils.getBaseHtml();
        html = html.replace("%head%", HtmlUtils.getMarketHead());
        StringBuilder data = new StringBuilder();

        data.append("<div class=\"filter\">");
        data.append("<a class=\"mock-ellipsis-item-cat\"").append(_filter.equals(Filter.All) ? " class=\"selected\" href=\"\"" : " href=\"filter All\"").append(">All</a>");
        data.append(" | ");
        data.append("<a class=\"mock-ellipsis-item-cat\"").append(_filter.equals(Filter.Below50) ? " class=\"selected\" href=\"\"" : " href=\"filter Below50\"").append(">Below 50$</a>");
        data.append(" | ");
        data.append("<a class=\"mock-ellipsis-item-cat\"").append(_filter.equals(Filter.Below25) ? " class=\"selected\" href=\"\"" : " href=\"filter Below25\"").append(">Below 25$</a>");
        data.append(" | ");
        data.append("<a class=\"mock-ellipsis-item-cat\"").append(_filter.equals(Filter.Below10) ? " class=\"selected\" href=\"\"" : " href=\"filter Below10\"").append(">Below 10$</a>");
        data.append(" | ");
        data.append("<a class=\"mock-ellipsis-item-cat\"").append(_filter.equals(Filter.Free) ? " class=\"selected\" href=\"\"" : " href=\"filter Free\"").append(">Only free</a>");
        data.append("</div>");
        data.append("<p align=\"center\" style=\"margin-top: 10\" style=\"font-family: Lato, Helvetica, Arial, sans-serif\">\n");
        data.append("<span style=\"font-size: 16px; color: #808080; text-shadow: 2px 2px #ff0000;\">");
        data.append(_currentCategory.getName());
        data.append("</span><br></p><br>");
        if (_currentCategory != null) {
            final int i[] = {0};

            data.append("<table class=\"asset-container\">");
            _currentCategory.getItems().values().stream().filter(item -> item.getPrice() <= _filter.getPrice()).forEach(item -> {
                if (i[0] >= _itemsPerPage)
                    return;
                if (i[0] % itemsInLine == 0 && i[0] > 0)
                    data.append("</tr>");
                if (i[0] % itemsInLine == 0) {
                    data.append("<tr>");
                }
                data.append("<td>");
                String asset = HtmlUtils.getAssetDiv();
                asset = asset.replaceAll("%category%", _currentCategory.getName());
                String name = HtmlUtils.findText(item.getName(), 195, HtmlUtils.FONT_TITLE);
                asset = asset.replaceAll("%title%", name);
                asset = asset.replaceAll("%image%", item.getThumbnail().getUrl());

                asset = parseData(new ArrayList<>(Arrays.asList("%lastDownload%", "%creator%", "%id%")), asset, item);

				/*if (SessionManager.getInstance().getUser().getOwnedAsset(item.getCatalogItemId()) != null) {
					asset = asset.replaceAll("%owned%", HtmlUtils.getAssetDivOwner());
					asset = asset.replaceAll("%price%", item.isCompatible(SessionManager.getInstance().getUser().getEngineVersion()) ? "" : "Not compatible");
				} else {
					asset = asset.replaceAll("%owned%", item.isCompatible(SessionManager.getInstance().getUser().getEngineVersion()) ? "" : "Not compatible");
					asset = asset.replaceAll("%price%", item.getPrice() == 0 ? "Free" : (item.getPrice() + " USD"));
				}*/
                data.append(asset);
                data.append("</td>");
                i[0]++;
            });
            if (i[0] % itemsInLine != 0)
                data.append("</tr>");
            data.append("</table>");
        }

        html = html.replace("%body%", data.toString());
        _textPane1.setText(html);
        if (_itemsPerPage == 8) {
            _textPane1.setCaretPosition(0);
            _scrollPane.getVerticalScrollBar().setValue(0);
        }
    }

}
