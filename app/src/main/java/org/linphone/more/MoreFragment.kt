package org.linphone.more

import android.content.res.Configuration
import android.os.Bundle
import com.google.android.material.transition.MaterialSharedAxis
import org.linphone.LinphoneApplication
import org.linphone.R
import org.linphone.activities.main.fragments.SecureFragment
import org.linphone.databinding.FragmentMoreBinding

class MoreFragment : SecureFragment<FragmentMoreBinding>() {
    override fun getLayoutId(): Int = R.layout.fragment_more

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        useMaterialSharedAxisXForwardAnimation = false
        if (LinphoneApplication.corePreferences.enableAnimations) {
            val portraitOrientation = resources.configuration.orientation != Configuration.ORIENTATION_LANDSCAPE
            val axis = if (portraitOrientation) MaterialSharedAxis.X else MaterialSharedAxis.Y
            enterTransition = MaterialSharedAxis(axis, true)
            reenterTransition = MaterialSharedAxis(axis, true)
            returnTransition = MaterialSharedAxis(axis, false)
            exitTransition = MaterialSharedAxis(axis, false)
        }
    }
}
