/*
 * Copyright (c) 2012. The Genome Analysis Centre, Norwich, UK
 * MISO project contacts: Robert Davey, Mario Caccamo @ TGAC
 * *********************************************************************
 *
 * This file is part of MISO.
 *
 * MISO is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MISO is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MISO.  If not, see <http://www.gnu.org/licenses/>.
 *
 * *********************************************************************
 */

package uk.ac.tgac.wis.controller;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sourceforge.fluxion.ajax.util.JSONUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.ServletContext;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Controller
public class MenuController implements ServletContextAware {

    ServletContext servletContext;

    @RequestMapping("/")
    public String indexPage() {
        return "/index.jsp";
    }

    @Override
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }


    @RequestMapping("/about")
    public String about() {
        return "/about.jsp";
    }

    @RequestMapping("/contact")
    public String contact() {
        return "/contact.jsp";
    }

    @RequestMapping("/indexer")
    public String indexer() {
        return "/indexer.jsp";
    }

    @RequestMapping("/services")
    public String services() {
        return "/services.jsp";
    }

    @RequestMapping("/blast")
    public String grassRootBlast() {
        return "/blast.jsp";
    }

    @RequestMapping("/hordeum_vulgare")
    public String grassRootBlast2() {
        return "/blast2.jsp";
    }

    @RequestMapping("/irods-files")
    public String irodsFiles(@RequestParam("key") String key, @RequestParam("value") String value) {

        JSONObject responseJSON = new JSONObject();
        StringBuilder esresult = new StringBuilder();

        String elasticsearch_url = "https://grassroots.tools/elastic-search/irods/_search?q=" + key + ":" + value;
        String redirectUrl = "https://opendata.earlham.ac.uk/wheat/views/list?ids=";


        try {
            HttpClient client = new DefaultHttpClient();
            HttpGet get = new HttpGet(elasticsearch_url);
            HttpResponse responseGet = client.execute(get);
            HttpEntity resEntityGet = responseGet.getEntity();
            if (resEntityGet != null) {
                BufferedReader rd = new BufferedReader(new InputStreamReader(resEntityGet.getContent()));
                String line = "";
                while ((line = rd.readLine()) != null) {
                    esresult.append(line);
                }
            }
            responseJSON = JSONObject.fromObject(esresult.toString());
            if (responseJSON != null && responseJSON.get("hits") != null) {
                JSONObject hitsJSON = responseJSON.getJSONObject("hits");
                if (hitsJSON != null && hitsJSON.get("total") != null)
                    if (hitsJSON.getInt("total") > 0) {
                        if (hitsJSON.get("hits") != null) {
                            JSONArray hitsArray = hitsJSON.getJSONArray("hits");
                            for (int i = 0; i < hitsArray.size(); i++) {
                                if (hitsArray.getJSONObject(i).get("_source") != null) {
                                    if (hitsArray.getJSONObject(i).getJSONObject("_source").get("irods_id") != null) {
                                        if (i == 0) {
                                            redirectUrl = redirectUrl + hitsArray.getJSONObject(i).getJSONObject("_source").getString("irods_id");
                                        } else {
                                            redirectUrl = redirectUrl + "," + hitsArray.getJSONObject(i).getJSONObject("_source").getString("irods_id");

                                        }
                                    }
                                }
                            }
                        }

                    }
            }

            return "redirect:" + redirectUrl;
        } catch (Exception e) {
            return "Failed: " + e.getMessage();
        }
    }

//    @RequestMapping("/elasticsearch")
//    public String elasticsearch(@RequestParam("key") String key, @RequestParam("value") String value) {
//
//        JSONObject responseJSON = new JSONObject();
//        StringBuilder esresult = new StringBuilder();
//
//        String elasticsearch_url = "https://grassroots.tools/elastic-search/_search?q=" + key + ":" + value;
//
//
//        try {
//            HttpClient client = new DefaultHttpClient();
//            HttpGet get = new HttpGet(elasticsearch_url);
//            HttpResponse responseGet = client.execute(get);
//            HttpEntity resEntityGet = responseGet.getEntity();
//            if (resEntityGet != null) {
//                BufferedReader rd = new BufferedReader(new InputStreamReader(resEntityGet.getContent()));
//                String line = "";
//                while ((line = rd.readLine()) != null) {
//                    esresult.append(line);
//                }
//            }
//            responseJSON = JSONObject.fromObject(esresult.toString());
//            if (responseJSON != null && responseJSON.get("hits") != null) {
//                JSONObject hitsJSON = responseJSON.getJSONObject("hits");
//                if (hitsJSON != null && hitsJSON.get("total") != null)
//                    if (hitsJSON.getInt("total") > 0) {
//                        if (hitsJSON.get("hits") != null) {
//                            JSONArray hitsArray = hitsJSON.getJSONArray("hits");
//                            for (int i = 0; i < hitsArray.size(); i++) {
//                                if (hitsArray.getJSONObject(i).get("_source") != null) {
//                                    if (hitsArray.getJSONObject(i).getJSONObject("_source").get("irods_id") != null) {
//                                        if (i == 0) {
////                                            redirectUrl = redirectUrl + hitsArray.getJSONObject(i).getJSONObject("_source").getString("irods_id");
//                                        } else {
////                                            redirectUrl = redirectUrl + "," + hitsArray.getJSONObject(i).getJSONObject("_source").getString("irods_id");
//
//                                        }
//                                    }
//                                }
//                            }
//                        }
//
//                    }
//            }
//
//            return "redirect:" + redirectUrl;
//        } catch (Exception e) {
//            return "Failed: " + e.getMessage();
//        }
//    }

    @RequestMapping("/eirods-dav-header/")
    public ModelAndView getHeaderforiRODSObj(@RequestParam("uuid") String uuid, ModelMap model) throws IOException {

        String elasticsearch_url = "https://opendata.earlham.ac.uk/projectinfo-min.json";

        String toronto = "All of the data listed here is available under the prepublication data sharing principle of the <a\n" +
                "            href=\"https://www.nature.com/articles/461168a\">Toronto agreement</a>.\n" +
                "        By using this data, you agree to:\n" +
                "\n" +
                "        <ul>\n" +
                "            <li>respect the rights of the data producers and contributors to analyze and publish the first\n" +
                "                global\n" +
                "                analyses and certain other reserved analyses of this data set in a peer-reviewed publication.\n" +
                "            </li>\n" +
                "            <li>not redistribute, release, or otherwise provide access to the data to anyone outside of the\n" +
                "                group, until\n" +
                "                the data has been published &amp; submitted to the public data repositories.\n" +
                "            </li>\n" +
                "            <li>contact the authors to discuss any plans to publish data or analyses that utilize this data to\n" +
                "                avoid the\n" +
                "                overlap of any planned analyses.\n" +
                "            </li>\n" +
                "            <li>fully cite the prepublication data along with any applicable versioning details.</li>\n" +
                "            <li>understand that this data as accessed is precompetitive and is not patentable in its present\n" +
                "                state.\n" +
                "            </li>\n" +
                "        </ul>\n" +
                "        This agreement does not expire by time but only upon publication of the first global analysis by the\n" +
                "        data\n" +
                "        producers and contributors.";


        JSONArray imetaArray = new JSONArray();
        JSONObject esObject = new JSONObject();

        try {
            String projectName = "";
            String poi = "";
            String license = "Toronto Agreement";
            String license_detail = toronto;
            String description = "";
            String license_style = "";
            String projectStyle = "style=\"padding: 100px 0px 0px 0px ! important;\"";
            if (uuid != null && !uuid.equals("null") && !uuid.equals("") && !uuid.equals("none")) {
                HttpClient client = new DefaultHttpClient();
                HttpGet esGet = new HttpGet(elasticsearch_url);
                HttpResponse responseGet = client.execute(esGet);
                HttpEntity resEntityGet = responseGet.getEntity();
                if (resEntityGet != null) {
                    BufferedReader rd = new BufferedReader(new InputStreamReader(resEntityGet.getContent()));
                    String line = "";
                    while ((line = rd.readLine()) != null) {
                        if (line.startsWith("[")) {
                            imetaArray = JSONArray.fromObject(line);

//                            esObject = JSONObject.fromObject(line);
                            for (int n = 0; n < imetaArray.size(); n++) {
                                esObject = imetaArray.getJSONObject(n);
                                if (esObject.getString("uuid").equals(uuid)) {
                                    JSONObject sourceObject = esObject;
                                    if (sourceObject.get("projectName") != null) {
                                        projectName = sourceObject.getString("projectName");
                                        projectStyle = "style=\"padding: 100px 0px 0px 0px ! important;\"";
                                    }
                                    if (sourceObject.get("authors") != null) {
                                        JSONArray authorArray = sourceObject.getJSONArray("authors");
//                                        String authors = "";
                                        for (int an = 0; an < authorArray.size(); an++) {
                                            if (an == 0) {
                                                poi = poi.concat(authorArray.getString(an));

                                            } else {
                                                poi = poi.concat(", " + authorArray.getString(an));

                                            }
                                        }
                                    }
                                    if (sourceObject.get("description") != null) {
                                        description = sourceObject.getString("description");
                                    }
                                    if (sourceObject.get("license") != null) {
                                        license = "Toronto Agreement";
                                        license_detail = toronto;

                                    } else {
                                        license_style = "display:none ! important; ";
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (uuid.equals("none")) {
                license = "";
                license_detail = "";
                license_style = "display:none ! important; ";
            }


            model.put("projectName", projectName);
            model.put("poi", poi);
            model.put("description", description);
            model.put("license", license);
            model.put("license_detail", license_detail);
            model.put("license_style", license_style);
            model.put("projectStyle", projectStyle);

            return new ModelAndView("/eirodsdavheader.jsp", model);

        } catch (
                Exception ex) {
            throw ex;
        }

    }

    @RequestMapping("/eirods-dav-header-test/")
    public ModelAndView getHeaderforiRODSObjTest(@RequestParam("uuid") String uuid, ModelMap model) throws IOException {

        String G_METADATA_API_URL_S = "https://grassroots.tools/test-insecure/data/api/metadata/";
        String elasticsearch_url = "https://grassroots.tools/elastic-search/irods/project/";

        String toronto = "All of the data listed here is available under the prepublication data sharing principle of the <a\n" +
                "            href=\"https://www.nature.com/articles/461168a\">Toronto agreement</a>.\n" +
                "        By using this data, you agree to:\n" +
                "\n" +
                "        <ul>\n" +
                "            <li>respect the rights of the data producers and contributors to analyze and publish the first\n" +
                "                global\n" +
                "                analyses and certain other reserved analyses of this data set in a peer-reviewed publication.\n" +
                "            </li>\n" +
                "            <li>not redistribute, release, or otherwise provide access to the data to anyone outside of the\n" +
                "                group, until\n" +
                "                the data has been published &amp; submitted to the public data repositories.\n" +
                "            </li>\n" +
                "            <li>contact the authors to discuss any plans to publish data or analyses that utilize this data to\n" +
                "                avoid the\n" +
                "                overlap of any planned analyses.\n" +
                "            </li>\n" +
                "            <li>fully cite the prepublication data along with any applicable versioning details.</li>\n" +
                "            <li>understand that this data as accessed is precompetitive and is not patentable in its present\n" +
                "                state.\n" +
                "            </li>\n" +
                "        </ul>\n" +
                "        This agreement does not expire by time but only upon publication of the first global analysis by the\n" +
                "        data\n" +
                "        producers and contributors.";


//            JSONArray imetaArray = new JSONArray();
        JSONObject esObject = new JSONObject();

        try {
            String projectName = "";
            String poi = "";
            String license = "";
            String license_detail = "";
            String description = "";
            String license_style = "display:none ! important; ";
            String projectStyle = "";
            if (uuid != null || !uuid.equals("null") || !uuid.equals("")) {
                HttpClient client = new DefaultHttpClient();
                HttpGet esGet = new HttpGet(elasticsearch_url + uuid);
                HttpResponse responseGet = client.execute(esGet);
                HttpEntity resEntityGet = responseGet.getEntity();
                if (resEntityGet != null) {
                    BufferedReader rd = new BufferedReader(new InputStreamReader(resEntityGet.getContent()));
                    String line = "";
                    while ((line = rd.readLine()) != null) {
                        esObject = JSONObject.fromObject(line);
                        if (esObject.get("_source") != null) {
                            JSONObject sourceObject = esObject.getJSONObject("_source");
                            if (sourceObject.get("projectName") != null) {
                                projectName = sourceObject.getString("projectName");
                                projectStyle = "style=\"padding: 100px 0px 0px 0px ! important;\"";
                            }
                            if (sourceObject.get("poi") != null) {
                                poi = sourceObject.getString("poi");
                            }
                            if (sourceObject.get("description") != null) {
                                description = sourceObject.getString("description");
                            }
                            if (sourceObject.get("license") != null) {
                                license = "License - " + sourceObject.getString("license");
                                license_style = "";
                                if (license.equals("License - toronto")) {
                                    license = "Toronto Agreement";
                                    license_detail = toronto;
                                }
                            } else {
                                license_style = "display:none ! important; ";
                            }

                        }
                    }
                }
            }
            model.put("projectName", projectName);
            model.put("poi", poi);
            model.put("description", description);
            model.put("license", license);
            model.put("license_detail", license_detail);
            model.put("license_style", license_style);
            model.put("projectStyle", projectStyle);

            return new ModelAndView("/eirodsdavheadertest.jsp", model);
        } catch (Exception ex) {
            throw ex;
        }
    }
}
