package com.example.smartairsetup.login;

/**
 * Presenter for login screen (handles business logic), note some business logic relating to
 * db is in the model. This is the only way that we can do JUnit tests.
 */
public class LoginPresenter {

    private final LoginView view;
    private final LoginModel model;
    private final EmailValidator emailValidator;

    public LoginPresenter(LoginView view, LoginModel model, EmailValidator emailValidator) {
        this.view = view;
        this.model = model;
        this.emailValidator = emailValidator;
    }

    public void handleSignIn(String identifier, String password) {
        view.clearError();

        if (identifier.isEmpty()) {
            view.showError("Email or username is required");
            return;
        }

        if (password.isEmpty()) {
            view.showError("Password is required");
            return;
        }

        if (!isPasswordStrong(password)) {
            view.showError("Password must be at least 8 characters, include a number, uppercase letter, and special character.");
            return;
        }

        view.enableSignInButton(false);

        if (identifier.contains("@")) {
            if (!emailValidator.isValid(identifier)) {
                view.showError("Enter a valid email");
                view.enableSignInButton(true);
                return;
            }

            model.signInParentOrProvider(identifier, password, new LoginModel.SignInCallback() {
                @Override
                public void onSuccess(String uid, String role) {
                    view.navigateToRoleHome(role);
                }

                @Override
                public void onFailure(String errorMessage) {
                    view.enableSignInButton(true);
                    view.showError(errorMessage);
                }
            });

        } else {
            model.signInChild(identifier, password, new LoginModel.ChildSignInCallback() {
                @Override
                public void onSuccess(String parentUid, String childDocId, boolean firstTime) {
                    if (firstTime) {
                        view.navigateToChildOnboarding(parentUid, childDocId);
                    } else {
                        view.navigateToChildHome(parentUid, childDocId);
                    }
                }

                @Override
                public void onFailure(String errorMessage) {
                    view.enableSignInButton(true);
                    view.showError(errorMessage);
                }
            });

        }
    }

    public void handleForgotPassword(String identifier) {
        view.clearError();

        if (identifier.isEmpty()) {
            view.showError("Enter your email to reset password");
            return;
        }

        if (!identifier.contains("@")) {
            view.showError("Password reset is only available for parent/provider emails.");
            return;
        }

        if (!emailValidator.isValid(identifier)) {
            view.showError("Enter a valid email");
            return;
        }

        model.sendPasswordResetEmail(identifier,
                () -> view.showToast("Password reset email sent. Check your inbox."),
                errorMessage -> view.showError(errorMessage));
    }

    private boolean isPasswordStrong(String password) {
        // At least 8 characters, 1 uppercase, 1 number, 1 special character
        if (password.length() < 8) return false;
        if (!password.matches(".*[A-Z].*")) return false;
        if (!password.matches(".*[0-9].*")) return false;
        if (!password.matches(".*[!@#$%^&*()].*")) return false;
        return true;
    }
}
