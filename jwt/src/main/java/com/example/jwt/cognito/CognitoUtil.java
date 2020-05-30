package com.example.jwt.cognito;

import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProviderClientBuilder;
import com.amazonaws.services.cognitoidp.model.*;
import com.medicea.mClear.marketClear.PartyMaster.UserMaster;
import com.medicea.mClear.marketClear.user.User;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class CognitoUtil {
    private class CognitoConfig{
          /*  String clientId = "7ml87ka4pu6o91l3qodbafa0eq";
            String userPoolId = "ap-south-1_hH0UXEwb5";
            String endpoint = "cognito-idp.ap-south-1.amazonaws.com";
            String region = "ap-south-1";
            String identityPoolId = "ap-south-1_ScJkZV43N";*/

        String clientId = "1fvs7hngqc8b9qgele4i9chvqb";
        String userPoolId = "ap-south-1_yyoVT7iI7";
        String endpoint = "cognito-idp.ap-south-1.amazonaws.com";
        String region = "ap-south-1";
        String identityPoolId = "ap-south-1_ScJkZV43N";

        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }

        public String getUserPoolId() {
            return userPoolId;
        }

        public void setUserPoolId(String userPoolId) {
            this.userPoolId = userPoolId;
        }

        public String getEndpoint() {
            return endpoint;
        }

        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint;
        }

        public String getRegion() {
            return region;
        }

        public void setRegion(String region) {
            this.region = region;
        }

        public String getIdentityPoolId() {
            return identityPoolId;
        }

        public void setIdentityPoolId(String identityPoolId) {
            this.identityPoolId = identityPoolId;
        }
    }
    CognitoConfig cognitoConfig = new CognitoConfig();
    public AWSCognitoIdentityProvider getAmazonCognitoIdentityClient() {
        ClasspathPropertiesFileCredentialsProvider  propertiesFileCredentialsProvider =
                new ClasspathPropertiesFileCredentialsProvider();

        return AWSCognitoIdentityProviderClientBuilder.standard()
                .withCredentials(propertiesFileCredentialsProvider)
                .withRegion(cognitoConfig.getRegion())
                .build();

    }
    public Object signUp(UserMaster userMaster){
        AWSCognitoIdentityProvider cognitoClient = getAmazonCognitoIdentityClient();
        AdminCreateUserRequest cognitoRequest = new AdminCreateUserRequest()
                .withUserPoolId(cognitoConfig.getUserPoolId())
                .withUsername(userMaster.getEmail())
                .withUserAttributes(
                        new AttributeType()
                                .withName("email")
                                .withValue(userMaster.getEmail()),
                        new AttributeType()
                                .withName("name")
                                .withValue(userMaster.getName()),
                        new AttributeType()
                                .withName("phone_number")
                                .withValue(userMaster.getMobile()),
                        new AttributeType()
                                .withName("email_verified")
                                .withValue("true"))
                .withTemporaryPassword("Test@5432")
                .withDesiredDeliveryMediums(DeliveryMediumType.EMAIL)
                .withForceAliasCreation(Boolean.FALSE);

        AdminCreateUserResult createUserResult =  cognitoClient.adminCreateUser(cognitoRequest);
        UserType cognitoUser =  createUserResult.getUser();
        AdminConfirmSignUpRequest adminConfirmSignUpRequest = new AdminConfirmSignUpRequest()
                .withUserPoolId(cognitoConfig.getUserPoolId())
                .withUsername(cognitoUser.getUsername());
        AdminConfirmSignUpResult result = cognitoClient.adminConfirmSignUp( adminConfirmSignUpRequest );
        return result;
    }
    public Object confirmUser(UserMaster userMaster){
        AWSCognitoIdentityProvider cognitoClient = getAmazonCognitoIdentityClient();
        AdminConfirmSignUpRequest adminConfirmSignUpRequest = new AdminConfirmSignUpRequest()
                .withUserPoolId(cognitoConfig.getUserPoolId())
                .withUsername(userMaster.getEmail());
        AdminConfirmSignUpResult result = cognitoClient.adminConfirmSignUp( adminConfirmSignUpRequest );
        return result;
    }

    public  AuthenticationResultType signIn(AuthenticationRequest authenticationRequest){
        AuthenticationResultType authenticationResult = null;
        AWSCognitoIdentityProvider cognitoClient = getAmazonCognitoIdentityClient();

        final Map <String, String> authParams = new HashMap <>();
        authParams.put("USERNAME", authenticationRequest.getUsername());
        authParams.put("PASSWORD", authenticationRequest.getPassword());

        final AdminInitiateAuthRequest authRequest = new AdminInitiateAuthRequest();
        authRequest.withAuthFlow(AuthFlowType.ADMIN_NO_SRP_AUTH)
                .withClientId(cognitoConfig.getClientId())
                .withUserPoolId(cognitoConfig.getUserPoolId())
                .withAuthParameters(authParams);
        AdminInitiateAuthResult result = cognitoClient.adminInitiateAuth(authRequest);
        cognitoClient.shutdown();
        return result.getAuthenticationResult();
    }
    public  Object fetchUsers(){
        AuthenticationResultType authenticationResult = null;
        AWSCognitoIdentityProvider cognitoClient = getAmazonCognitoIdentityClient();
        /** prepare Cognito list users request */
        ListUsersRequest listUsersRequest = new ListUsersRequest();
        listUsersRequest.withUserPoolId(cognitoConfig.getUserPoolId());
        listUsersRequest.setLimit(60);

        /** send list users request */
        ListUsersResult result = cognitoClient.listUsers(listUsersRequest);

        cognitoClient.shutdown();
        return result;
    }

}