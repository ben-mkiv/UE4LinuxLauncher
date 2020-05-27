package launcher;

import launcher.managers.MarketplaceManager;
import launcher.managers.SessionManager;
import launcher.objects.EpicCategory;
import launcher.objects.EpicImage;
import launcher.objects.EpicItem;
import launcher.objects.HtmlUtils;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.text.DefaultCaret;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

abstract public class AssetListForm extends JPanel {
    enum Filter {
        All(Double.MAX_VALUE),
        Below10(10),
        Below25(25),
        Below50(50),
        Free(0);

        double _price;

        Filter(double price) {
            _price = price;
        }

        public double getPrice() {
            return _price;
        }
    }

    JPanel mainPanel;
    JPanel _assetsControls;
    JComboBox _filterDownloaded;
    JComboBox _filterVendor;
    JComboBox _filterCategory;
    JTextField _filterText;
    JScrollPane _scrollPane;


    Thread _downloadThread;

    int _caretPosition = 0;

    Filter _filter = Filter.All;


    JTextPane _textPane1;

    int itemsInLine = 4;


    EpicCategory _currentCategory;

    int _itemsPerPage = 1024;
    boolean _viewingItem;
    boolean isReloading = false;

    enum OwnedAssetsFilter { ALL, DOWNLOADED, NOTDOWNLOADED };


    OwnedAssetsFilter ownedAssetsFilter = OwnedAssetsFilter.ALL;


    public AssetListForm(){
        super();

        _textPane1 = new JTextPane();
        _textPane1.setEditable(false);
        _textPane1.setContentType("text/html");
        DefaultCaret caret = (DefaultCaret) _textPane1.getCaret();
        caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);

        _scrollPane.setVerticalScrollBarPolicy(20);
        _scrollPane.setViewportView(_textPane1);

        ((HTMLEditorKit) _textPane1.getEditorKitForContentType("text/html")).setAutoFormSubmission(false);


        class ResizeListener extends ComponentAdapter {
            public void componentResized(ComponentEvent e) {
                int newItemsInLine = (int) Math.floor((e.getComponent().getWidth() - 50) / 267); // 50 is just an arbitrary picked number to count in the spacing left/right of the table
                if(itemsInLine != newItemsInLine) {
                    itemsInLine = newItemsInLine;
                    reloadList();
                }
            }
        }

        _scrollPane.addComponentListener(new ResizeListener());



        _textPane1.addHyperlinkListener(e -> {
            if (!e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED))
                return;

            if(e.getDescription().startsWith("ownedAssets")){
                String parameters = e.getDescription().substring("ownedAssets?".length());

                if(parameters.contains("filter=notdownloaded")) {
                    ownedAssetsFilter = OwnedAssetsFilter.NOTDOWNLOADED;
                    reloadList();
                    return;
                }
                else if(parameters.contains("filter=downloaded")) {
                    ownedAssetsFilter = OwnedAssetsFilter.DOWNLOADED;
                    reloadList();
                    return;
                }
                else  if(parameters.contains("filter=all")) {
                    ownedAssetsFilter = OwnedAssetsFilter.ALL;
                    reloadList();
                    return;
                }
            }


            if (e.getDescription().startsWith("item")) {
                _caretPosition = _textPane1.getCaretPosition();
                String data[] = e.getDescription().split((" "));
                if (data.length == 3)
                    showItemInfo(data[1], Integer.parseInt(data[2]));
                else
                    showItemInfo(data[1], 0);
                _viewingItem = true;
            } else if (e.getDescription().startsWith("download_item")) {
                startDownload(e.getDescription().split(" ")[1]);
            } else if (e.getDescription().equalsIgnoreCase("back")) {
                _viewingItem = false;
                reloadList();
                _textPane1.setCaretPosition(_caretPosition);
            } else if (e.getDescription().startsWith("filter")) {
                _filter = Filter.valueOf(e.getDescription().split(" ")[1]);
                _itemsPerPage = 8;
                reloadList();
            } else if (e.getDescription() != null && e.getDescription().length() > 0) {
                if (Desktop.isDesktopSupported()) {
                    try {
                        Desktop.getDesktop().browse(e.getURL().toURI());
                    } catch (IOException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    } catch (URISyntaxException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                }
            }
        });


    }


    private void showItemInfo(String catalogItemId, int startAt) {
        String html = HtmlUtils.getBaseHtml();
        html = html.replace("%head%", HtmlUtils.getAssetInfoHead());

        String data = "";

        EpicItem item = MarketplaceManager.getInstance().getItem(catalogItemId);

        if (item != null) {
            data = HtmlUtils.getAssetInfo();

            data = parseData(new ArrayList<>(Arrays.asList("%lastDownload%", "%title%", "%description%", "%longDescription%", "%techDescription%", "%totalSize%")), data, item);

            String firstImage = "";
            String images = "";
            String navigation = "";
            int photosPerPage = 3;
            System.out.println(item.getImages().size());
            if (item.getImages().size() > 0) {
                String images1 = "";
                int firstPhoto = Math.max(0, startAt + 1 == item.getImages().size() ? startAt - 2 : startAt - 1);
                int lastPhoto = Math.min(item.getImages().size(), startAt == 0 ? startAt + 3 : startAt + 2);
                for (int i = firstPhoto; i < lastPhoto; i++) {
                    if (i == startAt) {
                        firstImage = "<img src=\"" + item.getImages().get(i).getUrl() + "\" class=\"\" width=\"640\" height=\"360\">";
//						continue;
                    }
                    if (item.getImages().size() <= i)
                        break;

                    EpicImage image = item.getImages().get(i);
                    images1 += "<td " + (i == startAt ? "style=\"background-color: #cccccc\"" : "") + ">";
                    if (i == startAt)
                        images1 += "<img  src=\"" + image.getUrl() + "\" class=\"\" width=\"192\" height=\"108\"></a>";
                    else
                        images1 += "<a href=\"item " + catalogItemId + " " + i + "\"><img src=\"" + image.getUrl() + "\" class=\"\" width=\"192\" height=\"108\"></a>";
                    images1 += "</td>";
                }
                images += images1;
                if (startAt == 0)
                    navigation = "<table><tr><td style=\"width: 50px\"> </td>%images%<td style=\"width: 50px\"><a href=\"%nextBypass%\">Next</a></td></tr></table>";
                else if (startAt + 1 == item.getImages().size())
                    navigation = "<table><tr><td style=\"width: 50px\"><a href=\"%prevBypass%\">Prev</a></td>%images%<td style=\"width: 50px\"> </td></tr></table>";
                else
                    navigation = "<table><tr><td style=\"width: 50px\"><a href=\"%prevBypass%\">Prev</a></td>%images%<td style=\"width: 50px\"><a href=\"%nextBypass%\">Next</a></td></tr></table>";
            }
            data = data.replaceAll("%firstImage%", firstImage);
            data = data.replaceAll("%navigation%", navigation);
            String versions;
            double lowestVersion = Double.MAX_VALUE;
            double highestVersion = Double.MIN_VALUE;
            String lowVersion = "";
            String highVersion = "";
            if (item.getReleases() != null) {
                for (int i = 0; i < item.getReleases().size(); i++) {
                    if (lowestVersion > item.getReleases().get(i).getLowestVersion()) {
                        lowestVersion = item.getReleases().get(i).getLowestVersion();
                        lowVersion = item.getReleases().get(i).getCompatibility().get(lowestVersion);
                    }
                    if (highestVersion < item.getReleases().get(i).getHighestVersion()) {
                        highestVersion = item.getReleases().get(i).getHighestVersion();
                        highVersion = item.getReleases().get(i).getCompatibility().get(highestVersion);
                    }
                }
            }
            if (lowVersion.equalsIgnoreCase(highVersion))
                versions = lowVersion + "";
            else
                versions = lowVersion + " - " + highVersion;
            data = data.replaceAll("%versions%", versions);

            StringBuilder platforms = new StringBuilder();

            if (item.getReleases() != null) {
                if (!item.getReleases().isEmpty()) {
                    for (String platform : item.getReleases().get(0).getPlatforms())
                        platforms.append("<div class=\"text\">").append(platform).append("</div>");
                }
            }
            data = data.replaceAll("%platforms%", platforms.toString());

            data = data.replaceAll("%images%", images);

            String downloadButton;

            if (item.isOwned())
                downloadButton = "<div class=\"download-button\"><a href=\"download_item " + catalogItemId + "\" class=\"btn\">Download</a></div>";
            else
                downloadButton = "<div class=\"download-button\"><a href=\"https://www.unrealengine.com/marketplace/" + item.getUrlPart() + "\" class=\"btn\">Go to Website</a></div><br>Price: " + (item.getPrice() == 0 ? "Free" : (item.getPrice() + " USD"));
            data = data.replaceAll("%download%", downloadButton);

            String prevBypass = "item " + catalogItemId + " " + (startAt - 1);
            String nextBypass = "item " + catalogItemId + " " + (startAt + 1);

            if (data.contains("%prevBypass%"))
                data = data.replaceAll("%prevBypass%", prevBypass);
            if (data.contains("%nextBypass%"))
                data = data.replaceAll("%nextBypass%", nextBypass);

            data = data.replaceAll("%backBypass%", "back");
        }

        html = html.replace("%body%", data);
        _textPane1.setText(html);
        _textPane1.setCaretPosition(0);
        _scrollPane.getVerticalScrollBar().setValue(0);
    }


    // parse common used item placeholders (needle) in HTML template string (data)
    String parseData(String needle, String data, EpicItem item){
        switch(needle){
            case "%lastDownload%":
                long lastDownload = item.getLastDownloadTime(SessionManager.getInstance().getUser().getCurrentProject());
                if(lastDownload != -1) {
                    return data.replaceAll("%lastDownload%", "last download: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z").format(new Date(lastDownload * 1000)));
                } else {
                    return data.replaceAll("%lastDownload%", "never downloaded");
                }

            case "%id%": return data.replaceAll("%id%", item.getCatalogItemId());
            case "%title%":	return data.replaceAll("%title%", item.getName());
            case "%description%": return data.replaceAll("%description%", item.getDescription());
            case "%longDescription%": return data.replaceAll("%longDescription%", item.getLongDescription());
            case "%techDescription%": return data.replaceAll("%techDescription%", item.getTechnicalDetails());
            case "%creator%": return data.replaceAll("%creator%", item.getSellerName());
            case "%totalSize%": return data.replaceAll("%totalSize%", EpicItem.toBytes(item.getTotalSize()));
        }

        return needle;
    }

    // parse a bunch of placeholders
    String parseData(ArrayList<String> needles, String data, EpicItem item){
        for(String needle : needles)
            data = parseData(needle, data, item);

        return data;
    }






    private void startDownload(String catalogItemId) {
        if (SessionManager.getInstance().getUser().getCurrentProject() == null || SessionManager.getInstance().getUser().getCurrentProject().length() < 1) {
            JOptionPane.showMessageDialog(this, "You have to select project in library!", "No selected project!", JOptionPane.ERROR_MESSAGE);
            return;
        }
        EpicItem item = MarketplaceManager.getInstance().getItem(catalogItemId);
        if (item == null)
            return;

        if (!item.isCompatible(SessionManager.getInstance().getUser().getUnrealEngineVersion())) {
            int returnVal = JOptionPane.showConfirmDialog(this, "This asset pack is not compatible with your [" + SessionManager.getInstance().getUser().getUnrealEngineVersion() + "] engine version! Do you want to Download it anyways?", "Not compatible!", JOptionPane.WARNING_MESSAGE);

            if(returnVal == 2)
                return;
        }

        if (!item.isOwned())
            return;

        MainForm.getInstance().lockUI(true);
        _scrollPane.setEnabled(false);
        _textPane1.setEnabled(false);

        DownloadForm.getInstance().startDownloading(item, this);
        _downloadThread = new Thread(item::startDownloading);
        _downloadThread.start();
    }

    public void finishDownload() {
        MainForm.getInstance().lockUI(false);

        _scrollPane.setEnabled(true);
        _textPane1.setEnabled(true);
    }



    abstract void reloadList();


    public void setReloading(boolean state){
        isReloading = state;
    }


}
