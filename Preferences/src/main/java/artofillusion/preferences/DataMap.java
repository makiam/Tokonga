package artofillusion.preferences;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class DataMap extends HashMap<String, Object> {

    protected File file = null;

    protected Object semaphore = null;

    protected volatile boolean modified = false;

    public static final Object[] EMPTY_ARRAY = new Object[0];

    public static final float[] EMPTY_FLOAT_ARRAY = new float[0];

    public DataMap(Map<String, Object> defaults) {
        super(defaults);
        this.modified = true;
    }

    public void open(File file, Object semaphore) {
        this.file = file;
        this.semaphore = semaphore;
    }

    @SuppressWarnings("unchecked")
    public void load() throws IOException {
        if (this.file == null || !this.file.exists()) {
            return;
        }
        Exception err = null;

        try(InputStream is = new FileInputStream(this.file)) {
            Properties props = new Properties();
            props.load(is);
            super.putAll((Map)props);
        } catch (Exception e) {
            err = e;
        }
        if (err != null) {
            throw new IOException(err);
        }
    }

    public void reload() throws IOException {
        this.modified = false;
        load();
    }

    public void commit() throws IOException {
        if (this.file == null || !this.modified) {
            return;
        }
        Exception err = null;
        synchronized (this) {
            
            try(OutputStream os = new FileOutputStream(this.file)) {
                Properties props = new Properties();
                props.putAll(this);
                props.store(os, "Changes committed by DataMap");
                os.flush();
                this.modified = false;
            } catch (IOException e) {
                err = e;
            }
        }
        if (err != null) {
            throw new IOException(err);
        }
    }

    @Override
    public Object put(String key, Object value) {
        Object result = super.put(key, value);
        setModifiedAndNotify();
        return result;
    }

    @Override
    public void putAll(Map<? extends String, ? extends Object> data) {
        super.putAll(data);
        setModifiedAndNotify();
    }

    public void putString(String name, String value) {
        super.put(name, value);
        setModifiedAndNotify();
    }
    
    private void setModifiedAndNotify() {
        if(modified) return;
        modified = true;
        if(semaphore == null) return;

        synchronized (semaphore) { semaphore.notifyAll(); }
        
    }
    
    public String getString(String name) {
        return (String) get(name);
    }

    public void putBoolean(String name, boolean value) {
        putString(name, value ? "true" : "false");
    }

    public boolean getBoolean(String name) {
        return "true".equalsIgnoreCase(getString(name));
    }

    public void putInt(String name, int value) {
        putString(name, String.valueOf(value));
    }

    public int getInt(String name) {
        return (int) parseLong(getString(name));
    }

    public void putLong(String name, long value) {
        putString(name, String.valueOf(value));
    }

    public long getLong(String name) {
        return parseLong(getString(name));
    }

    public void putFloat(String name, float value) {
        putString(name, String.valueOf(value));
    }

    public float getFloat(String name) {
        return (float) parseDouble(getString(name));
    }

    public void putDouble(String name, double value) {
        putString(name, String.valueOf(value));
    }

    public double getDouble(String name) {
        return parseDouble(getString(name));
    }

    public void putArray(String name, Object array) {
        if (array == null) {
            putString(name, "[]");
            System.out.println("DatProperties.putArray: array is null");
            return;
        }
        Class<?> type = array.getClass().getComponentType();
        if (type == null) {
            System.out.println("DataProperties.putArray: value is not an array: " + array);
            return;
        }
        if (type == int.class) {
            putString(name, Arrays.toString((int[]) array));
        } else if (type == float.class) {
            putString(name, Arrays.toString((float[]) array));
        } else if (type == long.class) {
            putString(name, Arrays.toString((long[]) array));
        } else if (type == double.class) {
            putString(name, Arrays.toString((double[]) array));
        } else if (type == boolean.class) {
            putString(name, Arrays.toString((boolean[]) array));
        } else {
            putString(name, Arrays.toString((Object[]) array));
        }
    }

    public Object getArray(String name, Object array) {
        String val = getString(name);
        if (val == null || val.length() == 0) {
            if (array == null) {
                return EMPTY_ARRAY;
            }
            val = "";
        }
        int cut = val.indexOf('[');
        if (cut > -1) {
            val = val.substring(cut + 1);
        }
        cut = val.indexOf(']');
        if (cut > -1) {
            val = val.substring(0, cut);
        }
        String[] vals = val.split(", ?");
        if (array == null) {
            return vals;
        }
        int len = Array.getLength(array);
        Class<?> type = array.getClass().getComponentType();
        if (type == null) {
            System.out.println("DataMap.getArray: value is not an array: " + array);
            return vals;
        }
        if (type == String.class || type == Object.class) {
            if (len >= vals.length) {
                System.arraycopy(vals, 0, array, 0, len);
                return array;
            }
            return vals;
        }
        if (type == int.class) {
            int[] iarray = (len >= vals.length) ? (int[]) array : new int[len];
            for (int i = 0; i < iarray.length; i++) {
                if (i < vals.length) {
                    iarray[i] = (int) parseLong(vals[i].trim());
                } else {
                    iarray[i] = 0;
                }
            }
            return iarray;
        }
        if (type == float.class) {
            float[] farray = (len >= vals.length) ? (float[]) array : new float[len];
            for (int i = 0; i < farray.length; i++) {
                if (i < vals.length) {
                    farray[i] = (float) parseDouble(vals[i].trim());
                } else {
                    farray[i] = 0.0F;
                }
            }
            return farray;
        }
        if (type == long.class) {
            long[] larray = (len >= vals.length) ? (long[]) array : new long[len];
            for (int i = 0; i < larray.length; i++) {
                if (i < vals.length) {
                    larray[i] = parseLong(vals[i].trim());
                } else {
                    larray[i] = 0L;
                }
            }
            return larray;
        }
        if (type == double.class) {
            double[] darray = (len >= vals.length) ? (double[]) array : new double[len];
            for (int i = 0; i < darray.length; i++) {
                if (i < vals.length) {
                    darray[i] = parseDouble(vals[i].trim());
                } else {
                    darray[i] = 0.0D;
                }
            }
            return darray;
        }
        if (type == boolean.class) {
            boolean[] barray = (len >= vals.length) ? (boolean[]) array : new boolean[len];
            for (int i = 0; i < barray.length; i++) {
                if (i < vals.length) {
                    barray[i] = "true".equalsIgnoreCase(vals[i].trim());
                } else {
                    barray[i] = false;
                }
            }
            return barray;
        }
        System.out.println("DataMap.getArray: could not parse array type: " + type.getName());
        return array;
    }

    public void putColor(String name, Color value) {
        putArray(name, value.getComponents(null));
    }

    public Color getColor(String name) {
        String val = getString(name);
        if (val == null || val.length() == 0) {
            return Color.BLACK;
        }
        if (val.indexOf(',') < 0)
      try {
            return Color.decode(val);
        } catch (NumberFormatException e) {
            return Color.BLACK;
        }
        float[] comps = (float[]) getArray(name, EMPTY_FLOAT_ARRAY);
        if (comps.length < 3) {
            System.out.println("DataArray.getColor: too few components: " + comps.length);
            return Color.BLACK;
        }
        return new Color(comps[0], comps[1], comps[2], (comps.length > 3) ? comps[3] : 1.0F);
    }

    public static long parseLong(String val) {
        if (val == null || val.length() == 0) {
            return 0L;
        }
        long result = 0L;
        int sign = 1;
        int max = val.length();
        for (int i = 0; i < max; i++) {
            char c = val.charAt(i);
            if (result == 0L && (c == '-' || c == '+')) {
                sign = (c == '-') ? -1 : 1;
            } else if (Character.isDigit(c)) {
                result = result * 10L + Character.digit(c, 10);
            } else {
                break;
            }
        }
        return sign * result;
    }

    public static double parseDouble(String val) {
        if (val == null || val.length() == 0) {
            return 0.0D;
        }
        long result = 0L;
        long frac = 0L;
        int exp = 0;
        boolean mantissa = true;
        int sign = 1, esign = 1;
        int max = val.length();
        for (int i = 0; i < max; i++) {
            char c = val.charAt(i);
            if (result == 0L && (c == '-' || c == '+')) {
                sign = (c == '-') ? -1 : 1;
            } else if (frac == 0L && c == '.') {
                frac = 1L;
            } else if (mantissa == true && (c == 'e' || c == 'E')) {
                mantissa = false;
            } else if (Character.isDigit(c)) {
                if (mantissa) {
                    result = result * 10L + Character.digit(c, 10);
                    if (frac > 0L) {
                        frac *= 10L;
                    }
                } else if (exp == 0 && (c == '-' || c == '+')) {
                    esign = (c == '-') ? -1 : 1;
                } else {
                    exp = exp * 10 + Character.digit(c, 10);
                }
            } else {
                break;
            }
        }
        if (frac == 0L) {
            frac = 1L;
        }
        return Math.pow(10.0D, (esign * exp)) * sign * result / frac;
    }

    public DataMap() {
    }
}

