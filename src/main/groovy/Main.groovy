import org.apache.commons.io.FileUtils
import org.apache.commons.lang3.RandomStringUtils
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils
import org.jsoup.Jsoup

static void main(String[] args) {
    final def domain = "https://wallpapershome.com"
    final def baseURL = "${domain}/?page="
    final def END_PAGE = 100
    final def distinction = "downloads_${RandomStringUtils.random(10, true, true)}"

    final def httpClient = HttpClients.createDefault()
    final def target = "${System.getProperty("user.home")}/${distinction}"

    def cnt = 0
    for (i in 1..END_PAGE) {
        def url = "${baseURL}${i}"
        println "[Page] ${url}"
        def elements = Jsoup.connect(url).get()
                .select("div#pics-list a")
        elements.each { element ->
            def imagePageURL = "${domain}${element.attr("href")}"
            println imagePageURL
            def get = new HttpGet(imagePageURL)
            get.setHeader(
                    "User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.110 Safari/537.36"
            )
            def response = httpClient.execute(get)
            def result = EntityUtils.toString(response.entity, "UTF-8")
            def innerPageObj = Jsoup.parse(result)
            def imageURL = domain +
                    innerPageObj.selectFirst(".block-download__resolutions--6 a").attr("href")
//            println imageURL
            def file = new File("${target}/${cnt++}_${imageURL.split("/").last()}")
            try {
                println "[Download] Start download ${imageURL}"
                FileUtils.copyURLToFile(new URL(imageURL), file, 10000, 10000)
                println "[Download] ${imageURL} downloaded to ${file.absolutePath}"
            } catch (Exception e) {
                file.delete()
                cnt--
                println "[Download] ${imageURL} download failed"
                println e
            }
        }
    }
    httpClient.close()
}