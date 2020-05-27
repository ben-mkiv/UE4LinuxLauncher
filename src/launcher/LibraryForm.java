package launcher;

import launcher.managers.EngineManager;
import launcher.managers.SessionManager;

import javax.swing.*;
import java.io.File;

public class LibraryForm extends JPanel {
    private JButton _updateButton;
    private JList _projectsList;
    private JTextField _engineInstallDir;
    private JButton _selectButton;
    private JPanel _LibraryForm;
    private JPanel _engineLocation;

    private static LibraryForm instance;

    public LibraryForm() {
        super();
        /*_projectsList.addHyperlinkListener(e -> {
            if (!e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED))
                return;
            if (e.getDescription().startsWith("project")) {
                String[] data = e.getDescription().split((" "));

                MainForm.getInstance().setProject(data[1]);
                SessionManager.getInstance().getUser().setCurrentProject(data[1]);
                updateProjectsList();
            }
        });*/

        _selectButton.addActionListener(actionEvent -> {
            JFileChooser chooser;
            chooser = new JFileChooser();
            String location = SessionManager.getInstance().getUser().getUe4InstallLocation() == null || SessionManager.getInstance().getUser().getUe4InstallLocation().length() < 1 ? "." : SessionManager.getInstance().getUser().getUe4InstallLocation();
            chooser.setCurrentDirectory(new File(location));
            chooser.setDialogTitle("Select UE4 install directory");
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setAcceptAllFileFilterUsed(false);
            if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                SessionManager.getInstance().getUser().setUe4InstallLocation(chooser.getSelectedFile().toString());
                SessionManager.getInstance().getUser().save();
                setEngineInstallDir(chooser.getSelectedFile().toString());
                EngineManager.getInstance().readEngineData();
            }
        });

        _updateButton.addActionListener(actionEvent -> EngineManager.getInstance().readEngineData());

    }

    public void setEngineInstallDir(String dir) {
        _engineInstallDir.setText(dir);
    }

    public void updateProjectsList() {
        /*String html = "<html><head><style>a {" +
                "font-family: FreeSerif;" +
                "color: #0aaff1;" +
                "font-weight: 700;" +
                "font-size: 12px;" +
                "}</style></head><body>%s</body></html>";
        StringBuilder data = new StringBuilder("<p align=\"center\" style=\"margin-top: 10\" style=\"font-family: Lato, Helvetica, Arial, sans-serif\">\n" +
                "      <span style=\"font-size: 16px; color: #808080; text-shadow: 2px 2px #ff0000;\">Select available project</span><br>\n" +
                "    </p><br>");
        if (SessionManager.getInstance().getUser().getProjects().size() > 0) {
            data.append("<table style=\"text-align: center;\">");
            int i = 0;
            int elementsInRow = 4;
            for (String projectName : SessionManager.getInstance().getUser().getProjects().keySet()) {
                if (i % elementsInRow == 0)
                    data.append("<tr>");
                data.append("<td width=235 style=\"border: 1px solid #999999; text-align: center;");
                data.append("\"><a href=\"project ").append(projectName).append("\"");
                if (projectName.equals(SessionManager.getInstance().getUser().getCurrentProject()))
                    data.append(" style=\"color: #ffa64d !important;\"");
                data.append(">").append(projectName).append("</a>").append("</td>");
                i++;
                if (i % elementsInRow == 0)
                    data.append("</tr>");
            }
            if (i % elementsInRow != 0)
                data.append("</tr>");
            data.append("</table>");
        }
        html = html.replace("%s", data.toString());
        _projectsList.setText(html);*/

        for (String projectName : SessionManager.getInstance().getUser().getProjects().keySet()){
            _projectsList.add(new JLabel(projectName));
        }
    }


    public static LibraryForm getInstance(){
        if(instance == null){
            instance = new LibraryForm();
        }

        return instance;
    }

}
