// Disable Jenkins setup wizard for automated deployment
import jenkins.model.*
import jenkins.install.InstallState

def instance = Jenkins.getInstance()

// Mark setup as complete to skip wizard
if (!instance.installState.isSetupComplete()) {
    instance.setInstallState(InstallState.INITIAL_SETUP_COMPLETED)
}

instance.save()
println "[INIT] Setup wizard disabled"
