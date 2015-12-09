package bonvio.controllers;

/**
 * Created by alexggg99 on 07.12.15.
 */

import bonvio.configs.CustomUserDetailsService;
import bonvio.model.Repo.RoleRepo;
import bonvio.model.Repo.UserRepo;
import bonvio.model.Role;
import bonvio.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collection;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 * Created by Vano on 17.05.2015.
 */
@Controller
@RequestMapping("/login")
public class AuthController {

    private String VK_URL = "https://oauth.vk.com/authorize";
    private int CLIENT_ID = 5178375;
    private String REDIRECT_URL = "http://localhost:8011/login/callback/vk";

    private final String USER_AGENT = "Mozilla/5.0";

    @Autowired
    private UserRepo userRepo;
    @Autowired
    private RoleRepo roleRepo;
//
//    @Autowired
//    TokenService tokenService;

    public AuthController(){
        System.out.println("LoginController");
    }


    @RequestMapping(method = RequestMethod.GET)
    public String index(Model model) {
        model.addAttribute("user", new User());
        String s = VK_URL + "?client_id=" + CLIENT_ID + "&scope=notify,friends,offline&redirect_uri="+ REDIRECT_URL +"&display=popup&v=5.29&response_type=code";
        model.addAttribute("url", s);
        model.addAttribute("user", new User());
        return "login";
    }

    @RequestMapping(value = "/test", method = RequestMethod.GET)
    public String test() {
        return "test";
    }

//    @RequestMapping(value = "/callback/facebook", method = RequestMethod.GET)
//    public class FacebookController extends ExternalController implements Constants {
//        @ RequestMapping(value = "/registration", params = "code")
//        public ModelAndView registrationAccessCode(@ RequestParam("code") String code, HttpServletRequest request) throws Exception {
//            String authRequest = Utils.sendHttpRequest("GET", FACEBOOK_URL_ACCESS_TOKEN, new String[]{"client_id", "redirect_uri", "client_secret", "code"}, new String[]{FACEBOOK_API_KEY, FACEBOOK_URL_CALLBACK_REGISTRATION, FACEBOOK_API_SECRET, code});
//            String token = Utils.parseURLQuery(authRequest).get("access_token");
//            String tokenRequest = Utils.sendHttpRequest("GET", FACEBOOK_URL_ME, new String[]{"access_token"}, new String[]{token})
//            Map<String, Json> userInfoResponse = Json.read(tokenRequest).asJsonMap();
//            String email = userInfoResponse.get("email").asString().toLowerCase();
//            String id = userInfoResponse.get("id").asString();
//            //verifying ... is new? is email in DB?
//            //creating objects
//            Customer customer = new Customer();
//            customer.setEmail(email);
//            //...
//            customerer = (Customerer) userDAO.put(customer);
//            FacebookAuthUser user = new FacebookAuthUser();
//            user.setFirstName(firstName);
//            //...
//            user.setIdentificationName(id);
//            user.setToken(token);
//            user.setType(AuthenticationType.FACEBOOK);
//            user.setEnabled(true);
//            user.setAuthority(EnumSet.of(Authority.CUSTOMER));
//            user.setUser(customer);
//            authenticationDAO.put(user);
//            return new ModelAndView(new RedirectView("/registrate.complete", true, true, false));
//        }
//
//        @ RequestMapping(value = "/registration", params = "error_reason")
//        public ModelAndView registrationError(@ RequestParam("error_description") String errorDescription, HttpServletRequest request, HttpServletResponse response) {
//            //return client to registration page with errorDescription
//            return new ModelAndView(new RedirectView("/registrate", true, true, false));
//        }
//        //will signin and signinError
//    }

    @RequestMapping(value = "/callback/vk",method = RequestMethod.GET, params="code")
    public ModelAndView getCode(@RequestParam("code") String code) throws Exception {

        Integer idUser = null;

        System.out.println(code);
        if (code != null) {
            try {

                String ResponseCode;
                JSONObject jsonObj;

                ResponseCode = getAccessToken(code);
                jsonObj = serialaized(ResponseCode);

                String access_token = jsonObj.get("access_token").toString();
                String user_id = jsonObj.get("user_id").toString();

                ResponseCode = getUserVk(Integer.parseInt(user_id));
                jsonObj = serialaized(ResponseCode);


                String response = jsonObj.get("response").toString();

                response = response.substring(1, response.length()-1);

                System.out.println("==="+response);


                jsonObj = serialaized(response);

                String first_name = (String) jsonObj.get("first_name");
                String last_name = (String) jsonObj.get("last_name");
                long uid = (long) jsonObj.get("uid");

                //verifying ... is new? is IdVk in DB?
                User user = userRepo.findByIdVk(uid);
                User newUser = null;
                if(user == null){
                    newUser = new User(
                            "12345",
                           "vk_user",
                            first_name,
                            last_name,
                            uid
                    );
                    Role role = roleRepo.findOne(2);
                    newUser.authority = role;
                    userRepo.save(newUser);
                    user = newUser;
                }
//
//                VKuser vkUser = new VKuser();
//                vkUser.setFirstName(first_name);
//                vkUser.setLastName(last_name);
//                vkUser.setToken(access_token);
//                vkUser.setId(uid);
//                vkUser.setType(AuthorityType.VK);
//                vkUser.setEnabled(true);
//                vkUser.setAuthority(EnumSet.of(Authority.ADMIN));
//                vkUser.setUser(user==null?newUser:user);

//                SimpleAuthUser simpleAuthUser = new SimpleAuthUser();
//                simpleAuthUser.setUser(user==null?newUser:user);
//                simpleAuthUser.setPassword("123");

//                vkUserRepo.save(vkUser);

                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

                final Authentication authentication2 = authentication;

                final CustomUserDetailsService userDetailsService = new CustomUserDetailsService();

                final org.springframework.security.core.userdetails.UserDetails u = userDetailsService.getUser(user);

                Authentication trustedAuthentication = new Authentication () {
                    private String name = "vk_user";
                    private Object details = authentication2.getDetails();

                    private UserDetails user = u;
                    private boolean authenticated = true;
                    private Collection<? extends GrantedAuthority> authorities = user.getAuthorities();

                    @ Override
                    public String getName() {
                        return name;
                    }
                    @ Override
                    public Collection<? extends GrantedAuthority> getAuthorities() {
                        return authorities;
                    }
                    @ Override
                    public Object getCredentials() {
                        return user.getPassword();
                    }
                    @ Override
                    public Object getDetails() {
                        return details;
                    }
                    @ Override
                    public Object getPrincipal() {
                        return user;
                    }
                    @ Override
                    public boolean isAuthenticated() {
                        return authenticated;
                    }
                    @ Override
                    public void setAuthenticated(boolean authenticated) throws IllegalArgumentException {
                        this.authenticated = authenticated;
                    }

                };

                authentication = trustedAuthentication;
                SecurityContextHolder.getContext().setAuthentication(authentication);

                return new ModelAndView(new RedirectView("/login/test", true, true, false));

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return new ModelAndView("index");
    }

    // serialaized JSON
    public JSONObject serialaized(String ResponseCode) throws Exception {
        JSONParser parser = new JSONParser();
        Object obj = parser.parse(ResponseCode);
        return (JSONObject) obj;
    }


    // getAccessToken
    private String getAccessToken(String code) throws Exception {

        String url = "https://oauth.vk.com/access_token?" +
                "client_id=5178375&" +
                "client_secret=OAL8p9xNd2j3kBNO59x4&" +
                "redirect_uri="+ REDIRECT_URL +
                "&code=" + code;

        return send(url);

    }

    //getUserVk
    private String getUserVk(Integer idVk) throws Exception {

        String url = "https://api.vk.com/method/users.get?" +
                "user_ids=" + idVk;

        return send(url);

    }


    //send
    private String send(String url) throws Exception{
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        // optional default is GET
        con.setRequestMethod("GET");
        //add request header
        con.setRequestProperty("User-Agent", USER_AGENT);

        int responseCode = con.getResponseCode();
        System.out.println("\nSending 'GET' request to URL : " + url);
        System.out.println("Response Code : " + responseCode);

        HttpURLConnection httpConn = (HttpURLConnection)con;
        InputStream is;
        if (httpConn.getResponseCode() >= 400) {
            is = httpConn.getErrorStream();
        } else {
            is = httpConn.getInputStream();
        }

        StringBuffer response = null;
        try{
            System.out.println(new InputStreamReader(is).getEncoding());
            BufferedReader in = new BufferedReader(new InputStreamReader(is));
            String inputLine;
            response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
        }catch (Exception ex){
            System.out.println();
        }


        //print result
        System.out.println("print result: " + response.toString());
        return response.toString();
    }



}






