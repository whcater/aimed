package com.example;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class EnvReader {
    // 使用懒加载模式，在第一次获取环境变量时才去加载.env文件
    private static Map<String, String> envMap = new HashMap<>();

    // 私有构造函数，防止类被实例化，因为这个类的功能都是通过静态方法提供的
    private EnvReader() {
    }

    /**
     * 初始化方法，用于加载.env文件并解析其中的配置项到envMap中
     */
    public static void Initialize() {
        try {
            File file = new File(".env");
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                // 去除行两端的空白字符
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    // 如果是空行或者是以#开头的注释行，则跳过
                    continue;
                }
                int index = line.indexOf('=');
                if (index == -1 || index == line.length() - 1) {
                    // 如果没有找到=号，或者=号在最后一个位置（意味着value为空），则格式不对，输出提示信息并跳过该行
                    System.out.println("格式错误的配置行: " + line);
                    continue;
                }
                String key = line.substring(0, index).trim();
                String value = line.substring(index + 1).trim();
                // if (value.contains("=")) {
                //     // 如果value中包含=号，不符合配置格式要求，输出提示信息并跳过该行
                //     System.out.println("配置项 " + key + " 的值格式错误，不能包含=号: " + value);
                //     continue;
                // }
                envMap.put(key, value);
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            System.out.println("找不到.env文件");
        }
    }

    /**
     * 获取指定环境变量的值，如果envMap还未加载则先调用Initialize方法加载
     *
     * @param key 环境变量的键
     * @return 对应的环境变量的值，如果不存在则返回null
     */
    public static String getEnvVariable(String key) {
        if (envMap.isEmpty()) {
            Initialize();
        }
        return envMap.get(key);
    }
}