/*
 * Copyright 2019 Nicolas Maltais
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package mapleleaf.materialdesign.engine.ui.activities.dialogs

import android.os.Bundle
import mapleleaf.materialdesign.engine.R
import mapleleaf.materialdesign.engine.base.UniversalActivityBase

class ActivityCalcDialog : UniversalActivityBase() {

    override fun getLayoutResourceId() = R.layout.activity_calc_dialog

    override fun initializeComponents(savedInstanceState: Bundle?) {
        setToolbarTitle(getString(R.string.toolbar_title_activity_calc_dialog))
    }

}
