package services;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

public class GoogleAPIClient {

    // TODO move to config
    static final String CLIENT_ID = "1029227804631-mruhj3igh0sghcs8tkeu99va3nnc4gpo.apps.googleusercontent.com";
    static final String CLIENT_SECRET = "WbxqUQffNpoEohBtGEX_xNVL";


    private GoogleIdTokenVerifier verifier;

    private GoogleAPIClient(String idTokenString){
        HttpTransport transport = new NetHttpTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

        this.verifier = new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
                .setAudience(Collections.singletonList(CLIENT_ID))
                .build();
    }

    private static GoogleAPIClient instance;

    private static GoogleAPIClient getInstance(){
        return instance;
    }

    public static boolean verify(String idTokenString) throws GeneralSecurityException, IOException {

// (Receive idTokenString by HTTPS POST)

        GoogleIdToken idToken = getInstance().verifier.verify(idTokenString);
        if (idToken != null) {
            GoogleIdToken.Payload payload = idToken.getPayload();

            // Print user identifier
            String userId = payload.getSubject();
            System.out.println("User ID: " + userId);

            // Get profile information from payload
            String email = payload.getEmail();
            boolean emailVerified = payload.getEmailVerified();
            String name = (String) payload.get("name");
            String pictureUrl = (String) payload.get("picture");
            String locale = (String) payload.get("locale");
            String familyName = (String) payload.get("family_name");
            String givenName = (String) payload.get("given_name");
            return true;

        } else {
            System.out.println("Invalid ID token.");
            return false;
        }
    }
}
