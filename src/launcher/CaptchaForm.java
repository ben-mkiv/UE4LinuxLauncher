package launcher;

import launcher.managers.AuthenticationManager;

import javax.swing.*;

public class CaptchaForm extends JFrame {
    private static CaptchaForm _instance = null;

    private JTextField textField1;
    private JTextField textField2;
    private JButton confirmButton;
    private JPanel captchaForm;
    private JPanel captchaFormUserInput;

    public CaptchaForm(){
        super("UE4LinuxLauncher - Captcha Request");
        setContentPane(captchaForm);
        textField1.setText(AuthenticationManager.CAPTCHA_URL);
        confirmButton.addActionListener(actionEvent -> storeToken());
        setSize(500, 250);
    }

    public void storeToken(){
        AuthenticationManager.FUNCAPTCHA_TOKEN = textField2.getText().trim();
        setVisible(false);
        LoginForm.getInstance().loginError("");
        AuthenticationManager.getInstance().doLogin();
    }

    public synchronized static CaptchaForm getInstance() {
        if (_instance == null)
            _instance = new CaptchaForm();
        return _instance;
    }

    public void start() {
        setVisible(true);
    }

}
