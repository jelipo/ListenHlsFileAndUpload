package other;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by cao on 2017/2/27.
 */
public class GetConfigParm {
    private static Properties prop;

    /**
     * 初始化。获取config.properties
     */
    static {
        InputStream inStream = GetConfigParm.class.getClassLoader().getResourceAsStream("config.properties");
        prop = new Properties();
        try {
            prop.load(inStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获得config.properties中的key对应的value
     * @param key config.properties中的key
     * @return 返回value
     */
    public static String get(String key){
        return prop.getProperty(key);
    }
}
