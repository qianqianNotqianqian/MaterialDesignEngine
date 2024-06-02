package mapleleaf.materialdesign.engine.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import mapleleaf.materialdesign.engine.databinding.FragmentNotRootBinding
import mapleleaf.materialdesign.engine.permissions.CheckRootStatus

class FragmentOverViewNotRoot : Fragment() {
    private var binding: FragmentNotRootBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentNotRootBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding!!.btnRetry.setOnClickListener {
            CheckRootStatus(this.requireContext(), {
                if (this.activity != null) {
                    this.requireActivity().recreate()
                }
            }, false, null).forceGetRoot()
        }
    }

    companion object {
        fun createPage(): Fragment {
            return FragmentOverViewNotRoot()
        }
    }
}