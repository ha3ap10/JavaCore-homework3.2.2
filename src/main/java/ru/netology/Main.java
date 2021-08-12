package ru.netology;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class Main {

//    https://api.nasa.gov/planetary/apod?date=2021-08-09&api_key=m19UyEQduQH4k1Uh8J0rMMML0zpQfunoLNKsyPUs
    public static final String URI = "https://api.nasa.gov/planetary/apod?api_key=m19UyEQduQH4k1Uh8J0rMMML0zpQfunoLNKsyPUs";
    public static final ObjectMapper mapper = new ObjectMapper();
    static String dir = "Media";

    public static void main(String[] args) throws IOException {

        ApiNASA resp = responseFromURL(URI);

        String hdURL = resp.getHdurl();
        String url = resp.getUrl();

        String folder = dir + "/" + resp.getDate();

//        new File(dir).mkdir(); // Создать корневую папку
        new File(folder).mkdir();

        //Скачать изображение в HD, если есть
        if (hdURL != null) {
            String fileNameHD = hdURL.substring(resp.getUrl().lastIndexOf("/") + 1);
            saveMedia(hdURL, folder + "/" + fileNameHD);
        }

        //Скачать изображение или другое медиа
        String fileName = url.substring(resp.getUrl().lastIndexOf("/") + 1);
        saveMedia(url, folder + "/" + fileName);

        //Скачать описание в файл
        saveExplanation(folder, resp);

    }

    private static void saveMedia(String url, String path) {
        try (InputStream in = new BufferedInputStream(new URL(url).openStream());
             FileOutputStream os = new FileOutputStream(path);
             BufferedOutputStream bos = new BufferedOutputStream(os)) {
            int c;
            while ((c = in.read()) != -1) {
                bos.write(c);
            }
            bos.flush();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private static void saveExplanation(String folder, ApiNASA resp) {
        String path = folder + "//" + resp.getTitle().replace(":", "..") + ".txt";
        String text = resp.getTitle() + ".\n\n" + resp.getExplanation().replace("  ", "\n");

        try (FileOutputStream fileOutStream = new FileOutputStream(path)) {
            fileOutStream.write(text.getBytes());
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    public static ApiNASA responseFromURL(String url) throws IOException {
        CloseableHttpClient httpClient = HttpClientBuilder.create()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setConnectTimeout(5000)    // максимальное время ожидание подключения к серверу
                        .setSocketTimeout(30000)    // максимальное время ожидания получения данных
                        .setRedirectsEnabled(false) // возможность следовать редиректу в ответе
                        .build())
                .build();

        HttpGet request = new HttpGet(url);
        CloseableHttpResponse response = httpClient.execute(request);

        String body = new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);

        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        return mapper.readValue(body, new TypeReference<ApiNASA>() {});
    }
}

