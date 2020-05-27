package launcher;

import launcher.managers.EngineManager;
import launcher.managers.SessionManager;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;

public class LibraryForm extends JFrame {
    private JButton _updateButton;
    private JTextField _engineInstallDir;
    private JButton _selectButton;
    private JPanel _LibraryForm;
    private JPanel _engineLocation;

    private static LibraryForm instance;

    public LibraryForm() {
        super();

        setContentPane(_LibraryForm);

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

        ArrayList<String> options = new ArrayList<>();
        options.add("please select a project");
        options.addAll(SessionManager.getInstance().getUser().getProjects().keySet());

        MainForm.getInstance()._projectsList.removeAll();

        for(String element : options) {
            MainForm.getInstance()._projectsList.addItem(element);
        }

        // auto select current project
        if(SessionManager.getInstance().getUser().getCurrentProject().length() > 0) {
            MainForm.getInstance()._projectsList.setSelectedItem(SessionManager.getInstance().getUser().getCurrentProject());
        }
    }


    public static LibraryForm getInstance(){
        if(instance == null){
            instance = new LibraryForm();
        }

        return instance;
    }

}
