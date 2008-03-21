/*
 * Copyright 2000-2008 JetBrains s.r.o.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.plugins.scala.facet;

import com.intellij.facet.impl.ui.FacetTypeFrameworkSupportProvider;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.libraries.Library;
import org.jetbrains.plugins.scala.config.ScalaConfiguration;

/**
 * @author ilyas
 */
public class ScalaFacetSupportProvider extends FacetTypeFrameworkSupportProvider<ScalaFacet> {

  protected ScalaFacetSupportProvider() {
    super(ScalaFacetType.INSTANCE);
  }

  protected void setupConfiguration(ScalaFacet facet, ModifiableRootModel rootModel, String version) {
    final ScalaConfiguration configuration = ScalaConfiguration.getInstance();

    Library lib = configuration.getScalaLib();
    if (lib != null) {
      rootModel.addLibraryEntry(lib);
    }

  }
}
