/*
 *  Copyright 2004 Francois Guillet
 *  Changes copyright 2022-2023 by Maksim Khramov
 *  This program is free software; you can redistribute it and/or modify it under the
 *  terms of the GNU General Public License as published by the Free Software
 *  Foundation; either version 2 of the License, or (at your option) any later version.
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY
 *  WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 *  PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 */
package artofillusion.spmanager;

import artofillusion.ApplicationPreferences;
import artofillusion.ui.*;
import buoy.widget.*;
import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import javax.swing.*;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Description of the Class
 *
 * @author François Guillet
 * @created 20 mars 2004
 */
@Slf4j
public class SPMParameters {

    private static List<String> repositories;
    private static int currentRepository;

    /**
     * return the current filter map
     */
    @Getter
    private static Map<String, String> filters;

    @Getter @Setter
    private static boolean useProxy;

    /**
     * Gets the proxyHost attribute of the SPMParameters object
     *
     * @return The proxyHost value
     */
    @Getter @Setter
    private static String proxyHost = "";

    /**
     * Gets the proxyPort attribute of the SPMParameters object
     *
     * @return The proxyPort value
     */
    @Getter @Setter
    private static String proxyPort = "";

    /**
     * Gets the username attribute of the SPMParameters object
     *
     * @return The username value
     */
    @Getter @Setter
    private static String userName = "";

    /**
     * Gets the password attribute of the SPMParameters object
     *
     * @return The password value
     */
    @Getter
    private static String password = "";

    private static boolean changed;
    private static final StringEncrypter se = new StringEncrypter("SPMan8ger");
    private boolean useCache = true;

    /**
     * enum for filter values - in order of increasing restriction
     */
    public static final int DEFAULT = 0;
    public static final int ENABLE = 1;
    public static final int MARK = 2;
    public static final int CONFIRM = 3;
    public static final int DISABLE = 4;
    public static final int HIDE = 5;
    public static final int LAST_FILTER = 6;
    public static final int FILTER_MODULO = LAST_FILTER;

    public static final String[] FILTER_NAMES = {
        SPMTranslate.text("filtDefault"), SPMTranslate.text("filtEnable"),
        SPMTranslate.text("filtMark"), SPMTranslate.text("filtConfirm"),
        SPMTranslate.text("filtDisable"), SPMTranslate.text("filtHide"),
        "default", "enable", "mark", "confirm", "disable", "hide"
    };

    /**
     * Constructor for the SPMParameters object
     */
    public SPMParameters() {
        repositories = new Vector<>();

        repositories.add("https://aoisp.sourceforge.net/AoIRepository");

        filters = new HashMap<>();
        filters.put("beta", "mark");
        filters.put("earlyAccess", "confirm");
        filters.put("experimental", "hide");

        currentRepository = 0;

        loadPropertiesFile();
        initHttp();
    }

    /**
     * Description of the Method
     */
    private void loadPropertiesFile() {
        File oldFile = new File(System.getProperty("user.home"), ".spmanagerprefs");
        if (Files.exists(oldFile.toPath())) {
            log.info("Deleting the old .spmanagerprefs file");
            oldFile.delete();
        }
        Path pp = ApplicationPreferences.getPreferencesFolderPath().resolve("spmanagerprefs");
        if (Files.notExists(pp)) {
            savePropertiesFile();
            return;
        }
        try (InputStream in = new BufferedInputStream(Files.newInputStream(pp))) {
            Properties props = new Properties();
            props.load(in);
            parseProperties(props);
        } catch (IOException ioe) {
            log.atError().setCause(ioe).log("Unable to read properties: {}", ioe.getMessage());
        }

    }

    /**
     * Gets the repositories list
     *
     * @param forceUpdate Description of the Parameter
     */
    public void getRepositoriesList(boolean forceUpdate) {
        new Thread(() -> getThreadedRepositoriesList(forceUpdate)).start();
    }

    /**
     * Gets the threadedRepositoriesList attribute of the SPMParameters object
     *
     * @param forceUpdate Description of the Parameter
     */
    private void getThreadedRepositoriesList(boolean forceUpdate) {
        final BDialog dlg = new BDialog(SPManagerFrame.getInstance(), SPMTranslate.text("remoteStatus"),true);

        dlg.setEnabled(true);

        (new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(500);
                    if (dlg.isEnabled()) {
                        dlg.setContent(new BLabel(SPMTranslate.text("waiting")));

                        dlg.pack();
                        UIUtilities.centerWindow(dlg);
                        if (dlg.isEnabled()) {
                            dlg.setVisible(true);
                        }
                    }
                } catch (InterruptedException e) {
                }
            }
        }).start();

        boolean updated = false;

        URL repListURL = null;
        //try to get a new repositories definition file
        try {
            repListURL = new URL("https://aoisp.sourceforge.net/SPRepositories.txt");
        } catch (MalformedURLException me) {
            log.atError().setCause(me).log("Bad URL: {}", me.getMessage());
        }
        SPManagerFrame.getInstance().setRemoteStatusText(SPMTranslate.text("fetchingRepositoriesList") + " " + repListURL, -1);
        try {
            HttpURLConnection conn = (HttpURLConnection) repListURL.openConnection();

            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                new BStandardDialog("SPManager", new String[]{
                    SPMTranslate.text("noRepoList"),
                    conn.getResponseMessage()
                    + " (" + conn.getResponseCode() + ")",}, BStandardDialog.ERROR).showMessageDialog(SPManagerFrame.getInstance());

                return;
            }

            LineNumberReader rd = new LineNumberReader(new InputStreamReader(conn.getInputStream()));

            String repoName;
            boolean modified = true;
            String currentString = repositories.get(currentRepository);

            log.atInfo().log("Current repo: ({}): {}", currentRepository, currentString);

            int previous = currentRepository;
            currentRepository = 0;
            List<String> newRepositories = new Vector<>();
            while (true) {
                repoName = rd.readLine();
                if (repoName == null || repoName.isEmpty()) {
                    break;
                }

                if (repoName.startsWith("<DOC")) {
                    log.atDebug().log("Error retrieving repositories list.");
                    SPManagerFrame.getInstance().setRemoteStatusText(SPMTranslate.text("noRepoList"), -1);

                    currentRepository = previous;
                    return;
                }

                repoName = repoName.trim();
                if (repoName.endsWith("/")) {
                    repoName = repoName.substring(0, repoName.length() - 1);
                }

                log.atInfo().log("Repository name: {}<<", repoName);

                newRepositories.add(repoName);
                if (repoName.equals(currentString)) {
                    currentRepository = rd.getLineNumber() - 1;
                    log.atInfo().log("New current: {}", currentRepository);
                    modified = false;
                }
            }

            repositories = newRepositories;
            rd.close();

            if (modified) {
                SwingUtilities.invokeLater(SPManagerFrame.getInstance()::updatePanes);
                updated = true;
            }
        } catch (IOException e) {
            if (!((e instanceof UnknownHostException) || (e instanceof SocketException))) {
                log.atError().setCause(e).log("IO Error: {}", e.getMessage());
            }
            SPManagerFrame.getInstance().setRemoteStatusText(SPMTranslate.text("unknownRepositoriesHost", repListURL), -1);
        } finally {
            // close and dispose of the dialog
            dlg.setEnabled(false);
            dlg.setVisible(false);
            dlg.dispose();

            SPManagerFrame.getInstance().setRemoteStatusTextDuration(3000);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                log.atError().setCause(e).log("Thread interrupted: {}", e.getMessage());
            }
            if ((!updated) && forceUpdate) {
                SwingUtilities.invokeLater(SPManagerFrame.getInstance()::updatePanes);
            }

        }
    }

    /**
     * Description of the Method
     */
    private void savePropertiesFile() {
        Path pp = ApplicationPreferences.getPreferencesFolderPath().resolve("spmanagerprefs");
        try (OutputStream out = new BufferedOutputStream(Files.newOutputStream(pp))) {
            Properties props = newProperties();
            props.store(out, "Scripts & Plugins Manager Preferences File");
        } catch (IOException ex) {
            log.atError().setCause(ex).log("IO Error: {}", ex.getMessage());
        }

    }

    /**
     * Description of the Method
     *
     * @param p Description of the Parameter
     */
    private void parseProperties(Properties p) {
        int i = 0;
        String s = null;

        repositories.clear();
        currentRepository = 0;

        while (i != -1) {
            s = p.getProperty("URL_" + i);
            if (s == null) {
                i = -1;
            } else {
                s.trim();
                if (s.endsWith("/")) {
                    s = s.substring(0, s.length() - 1);
                }

                repositories.add(s);
                ++i;
            }
        }

        s = p.getProperty("default", "0");
        try {
            currentRepository = Integer.parseInt(s);
            if (currentRepository > repositories.size()) {
                currentRepository = 0;
            }
        } catch (NumberFormatException e) {
            currentRepository = 0;
            log.atError().log("SPManager : Wrong default URL index in properties file.");
        }

        p.forEach((Object pKey, Object value) -> {
            String key = (String) pKey;
            if (key.startsWith("FILTER_")) {
                filters.put(key.substring("FILTER_".length()), (String) value);
            }
        });

        // initialise an empty filter set
        if (filters.isEmpty()) {
            filters.put("beta", "mark");
            filters.put("earlyAccess", "confirm");
            filters.put("experimental", "hide");
        }

        proxyHost = p.getProperty("proxyHost", "");
        proxyPort = p.getProperty("proxyPort", "");
        userName = p.getProperty("username", "");
        password = se.decrypt(p.getProperty("password", ""));

        s = p.getProperty("useProxy", "false");
        try {
            useProxy = Boolean.parseBoolean(s);
        } catch (Exception e) {
            useProxy = false;
            log.atError().log("SPManager : Invalid use of proxy setting in properties file: useProxy={}", useProxy);
        }
        useCache = Boolean.parseBoolean(p.getProperty("usecache", "true"));

    }

    /**
     * Description of the Method
     *
     * @return Description of the Return Value
     */
    private Properties newProperties() {
        Properties p = new Properties();

        for (int i = 0; i < repositories.size(); ++i) {
            p.setProperty("URL_" + i, repositories.get(i));
        }
        p.setProperty("default", String.valueOf(currentRepository));

        filters.forEach((String key, String value) -> {
            p.setProperty("FILTER_" + key, value);
        });

        p.setProperty("proxyHost", proxyHost);
        p.setProperty("proxyPort", proxyPort);
        p.setProperty("username", userName);
        p.setProperty("password", se.encrypt(password));
        p.setProperty("useProxy", String.valueOf(useProxy));
        p.setProperty("usecache", String.valueOf(useCache));
        return p;
    }

    /**
     * Sets the uRLs attribute of the SPMParameters object
     *
     * @param urls The new uRLs value
     * @param selectedIndex The new uRLs value
     */
    public void setURLs(String[] urls, int selectedIndex) {
        repositories.clear();
        repositories.addAll(Arrays.asList(urls));
        currentRepository = selectedIndex;
        savePropertiesFile();
    }

    /**
     * Gets the repositories attribute of the SPMParameters object
     *
     * @return The repositories value
     */
    public String[] getRepositories() {
        return repositories.toArray(new String[0]);
    }

    /**
     * Gets the currentRepository attribute of the SPMParameters object
     *
     * @return The currentRepository value
     */
    public URL getCurrentRepository() {
        URL url = null;
        try {
            url = new URL(repositories.get(currentRepository));
        } catch (MalformedURLException me) {
            log.atError().setCause(me).log("Bad URL: {}", me.getMessage());
        }
        return url;
    }

    /**
     * Sets the currentRepository attribute of the SPMParameters object
     *
     * @param c The new currentRepository value
     */
    public void setCurrentRepository(int c) {
        currentRepository = c;
    }

    /**
     * Gets the currentRepositoryIndex attribute of the SPMParameters object
     *
     * @return The currentRepositoryIndex value
     */
    public int getCurrentRepositoryIndex() {
        if (currentRepository < 0) {
            getRepositoriesList(false);
        }
        return currentRepository;
    }

    /**
     * get a filter value
     */
    public int getFilter(String name) {
        return getFilterType(filters.get(name));
    }

    public String getFilterString(String name) {
        return filters.get(name);
    }

    /**
     * return the filter type corresponding to this filter value
     */
    public static int getFilterType(String val) {
        if (val == null || val.isEmpty()) {
            return DEFAULT;
        }

        for (int i = 0; i < FILTER_NAMES.length; i++) {
            if (val.equals(FILTER_NAMES[i])) {
                return i % FILTER_MODULO;
            }
        }

        return DEFAULT;
    }

    public static String getFilterType(int type) {
        if (type < 0) {
            return FILTER_NAMES[0];
        }
        return FILTER_NAMES[type % FILTER_MODULO];
    }

    public void addFilter(String name, int type) {
        filters.put(name, getFilterType(type));
    }

    /**
     * Description of the Method
     *
     * @return Description of the Return Value
     */
    public boolean useProxy() {
        return useProxy;
    }

    /**
     * Returns true if cached headers file is to be used
     *
     * @return The password value
     */
    public boolean getUseCache() {
        return useCache;
    }

    /**
     * Sets if cached headers files are to be used
     *
     * @param useCache True if cached info is to be used
     */
    public void setUseCache(boolean useCache) {
        this.useCache = useCache;
    }

    /**
     * Sets the proxyParameters attribute of the SPMParameters object
     *
     * @param useProxy The new proxyParameters value
     * @param proxyHost The new proxyParameters value
     * @param proxyPort The new proxyParameters value
     * @param userName The new proxyParameters value
     * @param password The new proxyParameters value
     */
    public void setProxyParameters(boolean useProxy, String proxyHost, String proxyPort, String userName, String password) {
        SPMParameters.useProxy = useProxy;
        SPMParameters.proxyHost = proxyHost;
        SPMParameters.proxyPort = proxyPort;
        SPMParameters.userName = userName;
        SPMParameters.password = password;
        initHttp();
        savePropertiesFile();
    }

    /**
     * Sets the changed attribute of the SPMParameters object
     *
     * @param ch The new changed value
     */
    public void setChanged(boolean ch) {
        changed = ch;
        if (changed) {
            savePropertiesFile();
        }
    }

    /**
     * Description of the Method
     *
     * @return Description of the Return Value
     */
    public boolean hasChanged() {
        return changed;
    }

    /**
     * Description of the Method
     */
    public void initHttp() {
        if (useProxy) {
            // set proxy host
            System.setProperty("http.proxyHost", proxyHost);
            // set proxy port
            System.setProperty("http.proxyPort", proxyPort);

            // set proxy authentication
            if (userName == null || userName.isEmpty()) {
                Authenticator.setDefault(new FirewallAuthenticator(null));
            } else {
                PasswordAuthentication pw = new PasswordAuthentication(userName, password.toCharArray());
                Authenticator.setDefault(new FirewallAuthenticator(pw));
            }
        } else {
            System.getProperties().remove("http.proxyHost");
            System.getProperties().remove("http.proxyPort");
            Authenticator.setDefault(null);
        }
    }

    //copied from jEdit
    /**
     * Description of the Class
     *
     * @author Francois Guillet
     * @created March, 20 2004
     */
    static class FirewallAuthenticator extends Authenticator {

        PasswordAuthentication pw;

        /**
         * Constructor for the FirewallAuthenticator object
         *
         * @param pw Description of the Parameter
         */
        public FirewallAuthenticator(PasswordAuthentication pw) {
            this.pw = pw;
        }

        /**
         * Gets the passwordAuthentication attribute of the
         * FirewallAuthenticator object
         *
         * @return The passwordAuthentication value
         */
        @Override
        protected PasswordAuthentication getPasswordAuthentication() {
            // if we have no stored credentials, prompt the user now
            if (pw == null) {
                BTextField nameField = new BTextField();
                BPasswordField pwField = new BPasswordField();
                ComponentsDialog dlg = new ComponentsDialog(SPManagerFrame.getInstance(), "SPManager:Authentication",
                        new Widget[]{nameField, pwField},
                        new String[]{Translate.text("SPManager:name"), Translate.text("SPManager.password")});

                if (dlg.clickedOk()) {
                    pw = new PasswordAuthentication(nameField.getText(), pwField.getText().toCharArray());
                }
            }

            return pw;
        }
    }
}
