package launcher;

import launcher.managers.EngineManager;
import launcher.managers.MarketplaceManager;
import launcher.managers.SessionManager;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.*;

public class MainForm extends JFrame {

	private enum ViewType {
		SplashScreen,
		Library,
		Marketplace,
		OwnedAssets
	}

	// main panel
	private JPanel mainPanel;

	// content holding panels
	private JPanel _navigationPanel, _headerPanel, _statusPanel, _mainPanel;

	private AssetListFormMarketplace MarketPlaceList;
	private AssetListFormOwned OwnedAssetsList;

	private JTextPane usernamePane;
	private JButton marketplaceButton;
	private JButton libraryButton;
	private JButton ownedAssetsButton;
	private JProgressBar _loadingProgressBar;
	private double _loadingPercent = 0;
	private JButton _logoutButton;
	private JLabel _engineVersion;

	private ViewType _viewType = ViewType.SplashScreen;
	private JButton _launchUE4Button;
	private JButton _reloadOwnedAssetsButton;
	public JComboBox<String> _projectsList;


	private String _mainLoadingBarText;
	private String _additionalLoadingBarText;



	MainForm() {
		super("UE4LinuxLauncher");

		setContentPane(mainPanel);
		setVisible(true);
		setResizable(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


		pack();


		OwnedAssetsList = new AssetListFormOwned();

		/*
		list1.addListSelectionListener(listSelectionEvent -> {
			if (_currentCategory == MarketplaceManager.getInstance().getCategory(list1.getSelectedValue().toString()) && !_viewingItem)
				return;
			_currentCategory = MarketplaceManager.getInstance().getCategory(list1.getSelectedValue().toString());
			_itemsPerPage = 8;
			_viewingItem = false;
			updateMarketplaceList();
		});

		marketplaceButton.addActionListener(actionEvent -> {
			if (_viewType.equals(ViewType.Marketplace) && !_viewingItem)
				return;
			marketplaceButton.setEnabled(false);
			switch (_viewType) {
				case OwnedAssets:
					ownedAssetsButton.setEnabled(true);
					break;
				case Library:
					libraryButton.setEnabled(true);
					break;
			}
			_libraryPanel.setVisible(false);
			_mainPanel.setVisible(true);
			_viewType = ViewType.Marketplace;
			list1.setVisible(true);
			_viewingItem = false;
			if (_currentCategory == null) {
				list1.setSelectedIndex(0);
				return;
			}
			updateMarketplaceList();

		});
		*/
		libraryButton.addActionListener(actionEvent -> showLibrary());

		ownedAssetsButton.addActionListener(actionEvent -> showOwnedAssets());

		_launchUE4Button.addActionListener(actionEvent -> launchUE4());

		_reloadOwnedAssetsButton.addActionListener(actionEvent -> reloadOwnedAssets());

		_logoutButton.addActionListener(actionEvent -> doLogout());


		_projectsList.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent var1) {
				if(_projectsList.getSelectedItem() instanceof String) {
					String value = (String) _projectsList.getSelectedItem();

					if(value.length() > 0 && !value.equals("please select a project")) {
						SessionManager.getInstance().getUser().setCurrentProject(value);
					}
				}
			}
		});

		marketplaceButton.setEnabled(false);

		showLibrary();
	}

	private void loadFormToMainPanel(JFrame panel){
		_mainPanel.removeAll();
		_mainPanel.setLayout(panel.getLayout());
		_mainPanel.add(panel.getContentPane());

		revalidate();

		lockUI(false);
	}

	private void showLibrary(){
		lockUI(true);
		_viewType = ViewType.Library;
		loadFormToMainPanel(LibraryForm.getInstance());
	}

	private void showOwnedAssets(){
		lockUI(true);
		_viewType = ViewType.OwnedAssets;
		loadFormToMainPanel(OwnedAssetsList);

		OwnedAssetsList.reloadList();
	}

	private void doLogout() {
		setVisible(false);
		LoginForm.getInstance().setLoginData(SessionManager.getInstance().getUser().getEmail(), "Password:");
		LoginForm.getInstance().clearError();
		LoginForm.getInstance().clearProgress();
		LoginForm.getInstance().allowActions();
		LoginForm.getInstance().setVisible(true);
	}

	public void updateCategoriesList(Collection<String> categories) {
		String[] data = new String[categories.size()];
		categories.toArray(data);
		for (int i = 0; i < data.length - 1; i++) {
			for (int j = i + 1; j < data.length; j++) {
				if (data[i].compareTo(data[j]) > 0) {
					String dd = data[i];
					data[i] = data[j];
					data[j] = dd;
				}
			}
		}

		// Todo: add marketplace form and move all this over there with its own list...
		//list1.setListData(data);
	}

	public void setLoadingText(String text) {
		setLoadingText(text, false);
	}

	public void setLoadingText(String text, boolean onlyText) {
		_additionalLoadingBarText = text;
		_loadingProgressBar.setString(_additionalLoadingBarText + (onlyText ? "" : " [" + String.format("%.2f", _loadingPercent) + "%]"));
	}

	public void increaseLoadingBar(int percent) {
		_loadingPercent = percent;
		_loadingProgressBar.setValue((int) _loadingPercent);
		_loadingProgressBar.setString(_additionalLoadingBarText + " [" + String.format("%.2f", _loadingPercent) + "%]");
	}

	public void increaseLoadingBar(double percent) {
		_loadingPercent = percent;
		_loadingProgressBar.setValue((int) _loadingPercent);
		_loadingProgressBar.setString(_additionalLoadingBarText + " [" + String.format("%.2f", _loadingPercent) + "%]");
	}

	public void hideLoading() {
		Thread t1 = new Thread(() -> {
			try {
				Thread.sleep(2000);
				_loadingProgressBar.setVisible(false);
			} catch (InterruptedException e) {
				// handle: log or throw in a wrapped RuntimeException
				throw new RuntimeException("InterruptedException caught in lambda", e);
			}
		});
		t1.start();
	}

	public void lockUI(boolean setLocked){
		if (setLocked || !_viewType.equals(ViewType.Marketplace))
			marketplaceButton.setEnabled(!setLocked);
		if (setLocked || !_viewType.equals(ViewType.Library))
			libraryButton.setEnabled(!setLocked);
		if (setLocked || !_viewType.equals(ViewType.OwnedAssets))
			ownedAssetsButton.setEnabled(!setLocked);


		_launchUE4Button.setEnabled(!setLocked);
		_reloadOwnedAssetsButton.setEnabled(!setLocked);

		// show loading bar when locked
		_loadingProgressBar.setVisible(setLocked);
	}



	public void setEngineVersion(double version) {
		if (version == 0) {
			_engineVersion.setText("None");
			_launchUE4Button.setEnabled(false);
			return;
		}
		_engineVersion.setText(version + "");
		_launchUE4Button.setEnabled(false); // TODO:
	}


	public MainForm setUsernamePane(String username) {
		usernamePane.setText(username);
		return this;
	}

	public void initialize() {
		LibraryForm.getInstance().setEngineInstallDir(SessionManager.getInstance().getUser().getUe4InstallLocation());

		lockUI(true);
		EngineManager.getInstance().readEngineData();
		MarketplaceManager.getInstance().createMarketplace();
//		SessionManager.getInstance().getUser().loadOwnedAssets();
		hideLoading();
		lockUI(false);
	}

	private static MainForm _instance = null;

	public synchronized static MainForm getInstance() {
		if (_instance == null)
			_instance = new MainForm();
		return _instance;
	}

	public synchronized static MainForm getInstance(boolean reinitialize) {
		if (reinitialize || _instance == null)
			_instance = new MainForm();
		return _instance;
	}

	private void launchUE4() {
		if(SessionManager.getInstance().getUser().getUe4InstallLocation().length() == 0 || SessionManager.getInstance().getUser().getCurrentProject().length() == 0){
			//Todo: add a warning that UE4 Location and Project have to be set!
			showLibrary();
			return;
		}

		new Thread(() -> {
			String launch = SessionManager.getInstance().getUser().getUe4InstallLocation() + "/Engine/Binaries/Linux/UE4Editor";
			String project = "";
			if (SessionManager.getInstance().getUser().getCurrentProject() != null && SessionManager.getInstance().getUser().getCurrentProject().length() > 0)
				project = SessionManager.getInstance().getUser().getProjects().get(SessionManager.getInstance().getUser().getCurrentProject()) + SessionManager.getInstance().getUser().getCurrentProject() + ".uproject";
			File file = new File(launch);
			file.setExecutable(true);
			try {
				Process p;
				if (project.isEmpty())
					p = new ProcessBuilder(launch, "&").start();
				else
					p = new ProcessBuilder(launch, project, "&").start();
				p.waitFor();
			} catch (Exception ignored) {
				ignored.printStackTrace();
			}
		}).start();
	}

	private void reloadOwnedAssets(){
		new Thread(() ->
		{
			OwnedAssetsList.setReloading(true);
			lockUI(true);
			MarketplaceManager.getInstance().loadOwnedAssets();
			OwnedAssetsList.reloadList();
			OwnedAssetsList.setReloading(false);
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			lockUI(false);
		}).start();
	}
}
