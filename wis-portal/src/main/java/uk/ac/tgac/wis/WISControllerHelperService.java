
package uk.ac.tgac.wis;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sourceforge.fluxion.ajax.Ajaxified;
import net.sourceforge.fluxion.ajax.util.JSONUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: bianx
 * Date: 02/11/11
 * Time: 15:59
 * To change this template use File | Settings | File Templates.
 */
@Ajaxified
public class WISControllerHelperService {


  public JSONObject searchSolr(HttpSession session, JSONObject json) {
    JSONObject response = new JSONObject();
    JSONObject jsonObject = new JSONObject();
    JSONArray jarray = new JSONArray();
    try {
      String searchStr = json.getString("searchStr");
      String solrSearch = "http://v0214.nbi.ac.uk:8983/solr/select?q=" + searchStr + "&wt=json";
      HttpClient client = new DefaultHttpClient();
      HttpGet get = new HttpGet(solrSearch);
      HttpResponse responseGet = client.execute(get);
      HttpEntity resEntityGet = responseGet.getEntity();
      if (resEntityGet != null) {
        BufferedReader rd = new BufferedReader(new InputStreamReader(resEntityGet.getContent()));
        String line = "";
        while ((line = rd.readLine()) != null) {
          jsonObject = JSONObject.fromObject(line);
        }
      }

      jarray = jsonObject.getJSONObject("response").getJSONArray("docs");

      response.put("numFound", jsonObject.getJSONObject("response").getInt("numFound"));
      response.put("docs", jarray);
      return response;
    }
    catch (Exception e) {
      return JSONUtils.SimpleJSONError("Failed: " + e.getMessage());
    }
  }

  public JSONObject searchElasticSearch(HttpSession session, JSONObject json) {
    JSONObject response = new JSONObject();
    String esresult = "";
    try {
      String name = json.getString("name");
      String value = json.getString("value");
      String esSearch = "http://v0214.nbi.ac.uk:9200/_search?q=" + name + ":" + value;
      HttpClient client = new DefaultHttpClient();
      HttpGet get = new HttpGet(esSearch);
      HttpResponse responseGet = client.execute(get);
      HttpEntity resEntityGet = responseGet.getEntity();
      if (resEntityGet != null) {
        BufferedReader rd = new BufferedReader(new InputStreamReader(resEntityGet.getContent()));
        String line = "";
        while ((line = rd.readLine()) != null) {
          esresult = line;
        }
      }


      response.put("json", esresult);
      return response;
    }
    catch (Exception e) {
      return JSONUtils.SimpleJSONError("Failed: " + e.getMessage());
    }
  }

  public JSONObject blastSearch(HttpSession session, JSONObject json) {
    JSONObject formJSON = json.getJSONObject("form");
    JSONObject blastResultJSON = json.getJSONObject("dummy");
    JSONObject response = new JSONObject();
    StringBuilder sb = new StringBuilder();
    try {
      JSONArray resultsHits = blastResultJSON.getJSONObject("BlastOutput").getJSONObject("report")
          .getJSONObject("results").getJSONObject("search").getJSONArray("hits");

      for (JSONObject hit : (Iterable<JSONObject>) resultsHits) {
        String id = hit.getJSONArray("description").getJSONObject(0).getString("id");
        String title = hit.getJSONArray("description").getJSONObject(0).getString("title");
        String taxid = hit.getJSONArray("description").getJSONObject(0).getString("taxid");
        String sciname = hit.getJSONArray("description").getJSONObject(0).getString("sciname");


        String bit_score = hit.getJSONArray("hsps").getJSONObject(0).getString("bit_score");
        String score = hit.getJSONArray("hsps").getJSONObject(0).getString("score");
        String evalue = hit.getJSONArray("hsps").getJSONObject(0).getString("evalue");
        String identity = hit.getJSONArray("hsps").getJSONObject(0).getString("identity");


        String query_from = hit.getJSONArray("hsps").getJSONObject(0).getString("query_from");
        String query_to = hit.getJSONArray("hsps").getJSONObject(0).getString("query_to");
        String hit_from = hit.getJSONArray("hsps").getJSONObject(0).getString("hit_from");
        String hit_to = hit.getJSONArray("hsps").getJSONObject(0).getString("hit_to");

        String query_strand = hit.getJSONArray("hsps").getJSONObject(0).getString("query_strand");
        String hit_strand = hit.getJSONArray("hsps").getJSONObject(0).getString("hit_strand");

        String qseq = hit.getJSONArray("hsps").getJSONObject(0).getString("qseq");
        String midline = hit.getJSONArray("hsps").getJSONObject(0).getString("midline");
        String hseq = hit.getJSONArray("hsps").getJSONObject(0).getString("hseq");
        sb.append("<div class='blastResultBox ui-corner-all'>");
        sb.append("<p><b>" + hit.getString("num") + ". Title</b>: " + title + " <a target=\"_blank\" href=\"http://www.ensembl.org/Multi/Search/Results?q=" + id + "\">Ensembl</a></p>");
        sb.append("<p><b>Sequence ID</b>: " + id + "</p>");
        sb.append("<p><b>Taxonomy ID</b>: " + taxid + " | <b>Scientific Name</b>: " + sciname + " | <b>Bit Score</b>: " + bit_score + "</p>");
        sb.append("<p><b>Score</b>: " + score + " | <b>Evalue</b>: " + evalue + " | <b>Identity</b>: " + identity + "</p><hr/>");
        sb.append("<p class='blastPosition'>Query from: " + query_from + " to: " + query_to + " Strand: " + query_strand + "</p>");
        sb.append(blastResultFormatter(qseq, midline, hseq, 100));
        sb.append("<p class='blastPosition'>Hit from: " + hit_from + " to: " + hit_to + " Strand: " + hit_strand + "</p>");
        sb.append("<hr/>");
        sb.append("</div>");
      }
      response.put("html", sb.toString());
      return response;
    }
    catch (Exception e) {
      return JSONUtils.SimpleJSONError("Failed: " + e.getMessage());
    }
  }


  public JSONObject checkV0214(HttpSession session, JSONObject json) {
    JSONArray formJSON = JSONArray.fromObject(json.get("form"));
    String sequence = "";
    int max_target_sequences = 100;
    Boolean short_queries = false;
    int expect_threshold = 10;
    int word_size = 28;
    int max_matches_query_range = 0;

    for (JSONObject j : (Iterable<JSONObject>) formJSON) {
      if (j.getString("name").equals("sequence")) {
        sequence = j.getString("value");
      }
      if (j.getString("name").equals("max_target_sequences")) {
        max_target_sequences = j.getInt("value");
      }
      if (j.getString("name").equals("short_queries")) {
        short_queries = j.getBoolean("value");
      }
      if (j.getString("name").equals("expect_threshold")) {
        expect_threshold = j.getInt("value");
      }
      if (j.getString("name").equals("word_size")) {
        word_size = j.getInt("value");
      }
      if (j.getString("name").equals("max_matches_query_range")) {
        max_matches_query_range = j.getInt("value");
      }
    }
    String service = "{\n" +
                     "  \"services\": [\n" +
                     "    {\n" +
                     "      \"services\": \"Blast service\",\n" +
                     "      \"run\": true,\n" +
                     "      \"parameter_set\": {\n" +
                     "        \"parameters\": [\n" +
                     "          {\n" +
                     "            \"param\": \"Input\",\n" +
                     "            \"current_value\": {\n" +
                     "              \"protocol\": \"\",\n" +
                     "              \"value\": \"\"\n" +
                     "            },\n" +
                     "            \"tag\": 1112100422,\n" +
                     "            \"type\": \"string\",\n" +
                     "            \"wheatis_type\": 7,\n" +
                     "            \"concise\": true\n" +
                     "          },\n" +
                     "          {\n" +
                     "            \"param\": \"Output\",\n" +
                     "            \"current_value\": {\n" +
                     "              \"protocol\": \"\",\n" +
                     "              \"value\": \"\"\n" +
                     "            },\n" +
                     "            \"tag\": 1112495430,\n" +
                     "            \"type\": \"string\",\n" +
                     "            \"wheatis_type\": 6,\n" +
                     "            \"concise\": true\n" +
                     "          },\n" +
                     "          {\n" +
                     "            \"param\": \"Query Sequence(s)\",\n" +
                     "            \"current_value\": \""+sequence+"\",\n" +
                     "            \"tag\": 1112626521,\n" +
                     "            \"type\": \"string\",\n" +
                     "            \"wheatis_type\": 5,\n" +
                     "            \"concise\": true\n" +
                     "          },\n" +
                     "          {\n" +
                     "            \"param\": \"Max target sequences\",\n" +
                     "            \"current_value\": "+max_target_sequences+",\n" +
                     "            \"tag\": 1112495430,\n" +
                     "            \"type\": \"integer\",\n" +
                     "            \"wheatis_type\": 2,\n" +
                     "            \"concise\": true\n" +
                     "          },\n" +
                     "          {\n" +
                     "            \"param\": \"Short queries\",\n" +
                     "            \"current_value\": "+short_queries.toString()+",\n" +
                     "            \"tag\": 1112754257,\n" +
                     "            \"type\": \"boolean\",\n" +
                     "            \"wheatis_type\": 0,\n" +
                     "            \"concise\": true\n" +
                     "          },\n" +
                     "          {\n" +
                     "            \"param\": \"Expect threshold\",\n" +
                     "            \"current_value\": "+expect_threshold+",\n" +
                     "            \"tag\": 1111840852,\n" +
                     "            \"type\": \"integer\",\n" +
                     "            \"wheatis_type\": 2,\n" +
                     "            \"concise\": true\n" +
                     "          },\n" +
                     "          {\n" +
                     "            \"param\": \"Word size\",\n" +
                     "            \"current_value\": "+word_size+",\n" +
                     "            \"tag\": 1113015379,\n" +
                     "            \"type\": \"integer\",\n" +
                     "            \"wheatis_type\": 2,\n" +
                     "            \"concise\": true\n" +
                     "          },\n" +
                     "          {\n" +
                     "            \"param\": \"Max matches in a query range\",\n" +
                     "            \"current_value\": "+max_matches_query_range+",\n" +
                     "            \"tag\": 1113015379,\n" +
                     "            \"type\": \"integer\",\n" +
                     "            \"wheatis_type\": 2,\n" +
                     "            \"concise\": true\n" +
                     "          }\n" +
                     "        ]\n" +
                     "      }\n" +
                     "    }\n" +
                     "  ]\n" +
                     "}\n" +
                     "\n";
    JSONObject responses = new JSONObject();
    try {

      String url = "http://n79610.nbi.ac.uk:8080/wheatis";


      HttpClient httpClient = new DefaultHttpClient();

      try {
        HttpPost request = new HttpPost(url);
        StringEntity params = new StringEntity(service);
        request.addHeader("content-type", "application/x-www-form-urlencoded");
        request.setEntity(params);
        HttpResponse response = httpClient.execute(request);

        ResponseHandler<String> handler = new BasicResponseHandler();
        String body = handler.handleResponse(response);
        System.out.println(response + "body" + body);


        responses.put("html", body);
      }
      catch (Exception e) {
        e.printStackTrace();
        return null;
      }
      finally {
        httpClient.getConnectionManager().shutdown();
      }
      return responses;
    }
    catch (Exception e) {
      return JSONUtils.SimpleJSONError("Failed: " + e.getMessage());
    }
  }

  public String blastResultFormatter(String qseq, String midline, String hseq, int size) {
    ArrayList<String> qseqList = splitEqually(qseq, size);
    ArrayList<String> midlineList = splitEqually(midline, size);
    ArrayList<String> hseqList = splitEqually(hseq, size);

    StringBuilder sb = new StringBuilder();

    if (qseqList.size() == midlineList.size() && qseqList.size() == hseqList.size()) {
      for (int i = 0; i < qseqList.size(); i++) {
        sb.append("<pre>" + qseqList.get(i) + "</pre>");
        sb.append("<pre>" + midlineList.get(i) + "</pre>");
        sb.append("<pre>" + hseqList.get(i) + "</pre>");
      }
      return sb.toString();
    }
    else {
      return "strands don't match";
    }
  }

  public static ArrayList<String> splitEqually(String text, int size) {
    ArrayList<String> list = new ArrayList<String>((text.length() + size - 1) / size);
    for (int start = 0; start < text.length(); start += size) {
      list.add(text.substring(start, Math.min(text.length(), start + size)));
    }
    return list;
  }
}
