/*
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.firebase.ui.auth.ui.email;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.BuildConfig;
import com.firebase.ui.auth.R;
import com.firebase.ui.auth.provider.FacebookProvider;
import com.firebase.ui.auth.provider.GoogleProvider;
import com.firebase.ui.auth.provider.IDPProvider;
import com.firebase.ui.auth.provider.IDPProviderParcel;
import com.firebase.ui.auth.provider.IDPResponse;
import com.firebase.ui.auth.ui.AcquireEmailHelper;
import com.firebase.ui.auth.ui.ActivityHelper;
import com.firebase.ui.auth.ui.AppCompatBase;
import com.firebase.ui.auth.ui.ExtraConstants;
import com.firebase.ui.auth.ui.FlowParameters;
import com.firebase.ui.auth.ui.TaskFailureLogger;
import com.firebase.ui.auth.ui.account_link.SaveCredentialsActivity;
import com.firebase.ui.auth.ui.email.field_validators.EmailFieldValidator;
import com.firebase.ui.auth.ui.email.field_validators.RequiredFieldValidator;
import com.firebase.ui.auth.ui.idp.CredentialSignInHandler;
import com.firebase.ui.auth.ui.idp.IDPBaseActivity;
import com.firebase.ui.auth.util.FirebaseAuthWrapperFactory;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.ArrayList;
import java.util.List;

public class SignInActivity extends IDPBaseActivity
        implements IDPProvider.IDPCallback, View.OnClickListener {
    private static final String TAG = "SignInActivity";
    private EditText mEmailEditText;
    private EditText mPasswordEditText;
    private EmailFieldValidator mEmailValidator;
    private RequiredFieldValidator mPasswordValidator;
    private ImageView mTogglePasswordImage;
    private AcquireEmailHelper mAcquireEmailHelper;

    private static final int RC_EMAIL_FLOW = 2;
    private static final int RC_ACCOUNT_LINK = 3;
    private static final int RC_SAVE_CREDENTIAL = 4;
    public static final int RC_REGISTER_ACCOUNT = 14;
    private ArrayList<IDPProvider> mIdpProviders;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAcquireEmailHelper = new AcquireEmailHelper(mActivityHelper);

        setTitle(R.string.sign_in);
        setContentView(R.layout.sign_in_layout);

        populateIdpList(mActivityHelper.getFlowParams().providerInfo);

        mEmailValidator = new EmailFieldValidator((TextInputLayout) findViewById(R.id
                .email_layout));

        mEmailEditText = (EditText) findViewById(R.id.email);


        int logoId = mActivityHelper.getFlowParams().logoId;
        ImageView logo = (ImageView) findViewById(R.id.logo);
        if (logoId == AuthUI.NO_LOGO) {
            logo.setVisibility(View.GONE);
        } else {
            logo.setImageResource(logoId);
        }

        TypedValue visibleIcon = new TypedValue();
        TypedValue slightlyVisibleIcon = new TypedValue();

        getResources().getValue(R.dimen.visible_icon, visibleIcon, true);
        getResources().getValue(R.dimen.slightly_visible_icon, slightlyVisibleIcon, true);

        mPasswordEditText = (EditText) findViewById(R.id.password);
        mTogglePasswordImage = (ImageView) findViewById(R.id.toggle_visibility);

        mPasswordEditText.setOnFocusChangeListener(new ImageFocusTransparencyChanger(
                mTogglePasswordImage,
                visibleIcon.getFloat(),
                slightlyVisibleIcon.getFloat()));

        mTogglePasswordImage.setOnClickListener(new PasswordToggler(mPasswordEditText));


        mPasswordValidator = new RequiredFieldValidator((TextInputLayout) findViewById(R.id
                .password_layout));

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        Button signInButton = (Button) findViewById(R.id.button_done);
        Button signUpButton = (Button) findViewById(R.id.signup_button);
        TextView recoveryButton =  (TextView) findViewById(R.id.trouble_signing_in);


        signUpButton.setOnClickListener(this);
        signInButton.setOnClickListener(this);
        recoveryButton.setOnClickListener(this);



    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mAcquireEmailHelper.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_EMAIL_FLOW) {
            if (resultCode == RESULT_OK) {
                finish(RESULT_OK, new Intent());
            }
        } else if (requestCode == RC_SAVE_CREDENTIAL) {
            finish(RESULT_OK, new Intent());
        } else if (requestCode == RC_ACCOUNT_LINK) {
            finish(resultCode, new Intent());
        }else if(requestCode == RC_REGISTER_ACCOUNT){
            Toast.makeText(SignInActivity.this,"signinpw_onactres" ,
                    Toast.LENGTH_SHORT).show();
            finish(resultCode, new Intent());
        }
        else {
            for(IDPProvider provider : mIdpProviders) {
                provider.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    @Override
    public void onBackPressed(){
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        System.exit(1);
    }

    private void signIn(String email, final String password) {
        mActivityHelper.getFirebaseAuth()
                .signInWithEmailAndPassword(email, password)
                .addOnFailureListener(
                        new TaskFailureLogger(TAG, "Error signing in with email and password"))
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        mActivityHelper.dismissDialog();
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = task.getResult().getUser();
                            if (FirebaseAuthWrapperFactory.getFirebaseAuthWrapper(
                                    mActivityHelper.getAppName())
                                    .isPlayServicesAvailable(SignInActivity.this)) {
                                Intent saveCredentialIntent =
                                        SaveCredentialsActivity.createIntent(
                                                SignInActivity.this,
                                                mActivityHelper.getFlowParams(),
                                                firebaseUser.getDisplayName(),
                                                firebaseUser.getEmail(),
                                                password,
                                                null,
                                                null);
                                startActivity(saveCredentialIntent);
                                finish(RESULT_OK, new Intent());
                            }
                        } else {
                            TextInputLayout passwordInput =
                                    (TextInputLayout) findViewById(R.id.password_layout);
                            passwordInput.setError(
                                    getString(com.firebase.ui.auth.R.string.login_error));
                        }
                    }
                });
    }

    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.button_done) {
            boolean emailValid = mEmailValidator.validate(mEmailEditText.getText());
            boolean passwordValid = mPasswordValidator.validate(mPasswordEditText.getText());
            if (!emailValid || !passwordValid) {
                return;
            } else {
                mActivityHelper.showLoadingDialog(R.string.progress_dialog_signing_in);
                signIn(mEmailEditText.getText().toString(), mPasswordEditText.getText().toString());
                return;
            }
        } else if (view.getId() == R.id.trouble_signing_in) {
            startActivity(RecoverPasswordActivity.createIntent(
                    this,
                    mActivityHelper.getFlowParams(),
                    mEmailEditText.getText().toString()));
            return;
        }else if(view.getId() == R.id.signup_button){
            startActivity(RegisterEmailActivity.createIntent(
                    mActivityHelper.getApplicationContext(),
                    mActivityHelper.getFlowParams(),
                    null));
          /*  Intent registerIntent = RegisterEmailActivity.createIntent(
                    mActivityHelper.getApplicationContext(),
                    mActivityHelper.getFlowParams(),
                    null);
            mActivityHelper.startActivityForResult(registerIntent, RC_REGISTER_ACCOUNT);*/
        }
    }

    public static Intent createIntent(
            Context context,
            FlowParameters flowParams,
            String email) {
        return ActivityHelper.createBaseIntent(context, SignInActivity.class, flowParams)
                .putExtra(ExtraConstants.EXTRA_EMAIL, email);
    }

    private void populateIdpList(List<IDPProviderParcel> providers) {
        mIdpProviders = new ArrayList<>();
        for (IDPProviderParcel providerParcel : providers) {
            switch (providerParcel.getProviderType()) {
                case FacebookAuthProvider.PROVIDER_ID :
                    mIdpProviders.add(new FacebookProvider(this, providerParcel));
                    break;
                case GoogleAuthProvider.PROVIDER_ID:
                    mIdpProviders.add(new GoogleProvider(this, providerParcel, null));
                    break;

                default:
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "Encountered unknown IDPProvider parcel with type: "
                                + providerParcel.getProviderType());
                    }
            }
        }
        LinearLayout btnHolder = (LinearLayout) findViewById(R.id.btn_holder);
        for (final IDPProvider provider: mIdpProviders) {
            View loginButton = null;
            switch (provider.getProviderId()) {
                case GoogleAuthProvider.PROVIDER_ID:
                    loginButton = getLayoutInflater()
                            .inflate(R.layout.idp_button_google, btnHolder, false);
                    break;
                case FacebookAuthProvider.PROVIDER_ID:
                    loginButton = getLayoutInflater()
                            .inflate(R.layout.idp_button_facebook, btnHolder, false);
                    break;
                default:
                    Log.e(TAG, "No button for provider " + provider.getProviderId());
            }
            if (loginButton != null) {
                loginButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mActivityHelper.showLoadingDialog(R.string.progress_dialog_loading);
                        provider.startLogin(SignInActivity.this);
                    }
                });
                provider.setAuthenticationCallback(this);
                btnHolder.addView(loginButton, 0);
            }
        }
    }


    @Override
    public void onSuccess(final IDPResponse response) {
        AuthCredential credential = createCredential(response);
        final FirebaseAuth firebaseAuth = mActivityHelper.getFirebaseAuth();

        firebaseAuth
                .signInWithCredential(credential)
                .addOnFailureListener(
                        new TaskFailureLogger(TAG, "Firebase sign in with credential unsuccessful"))
                .addOnCompleteListener(new CredentialSignInHandler(
                        SignInActivity.this,
                        mActivityHelper,
                        RC_ACCOUNT_LINK,
                        RC_SAVE_CREDENTIAL,
                        response));
    }
    @Override
    public void onFailure(Bundle extra) {
        // stay on this screen
        mActivityHelper.dismissDialog();
    }
}
