/**
 * Copyright 2018 The original authors.
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
package io.dekorate.knative.generator;

import io.dekorate.Session;
import io.dekorate.SessionWriter;
import io.dekorate.deps.kubernetes.api.model.KubernetesList;
import io.dekorate.deps.knative.serving.v1beta1.Service;
import io.dekorate.knative.annotation.KnativeApplication;
import io.dekorate.processor.SimpleFileWriter;
import io.dekorate.project.FileProjectFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

class KnativeApplicationGeneratorTest {
  static Path tempDir;

  @BeforeAll
  public static void setup() throws IOException {
    tempDir = Files.createTempDirectory("dekorate");
  }

  @Test
  public void shouldGenerateKnativeAndWriteToTheFilesystem()  {
    SessionWriter writer = new SimpleFileWriter(tempDir);
    Session session = Session.getSession();
    session.setWriter(writer);

    KnativeApplicationGenerator generator = new KnativeApplicationGenerator() {};
    generator.setProject(FileProjectFactory.create(new File(".")));
    System.out.println("Project root:" + generator.getProject());

    Map<String, Object> map = new HashMap<String, Object>() {{
      put(KnativeApplication.class.getName(), new HashMap<String, Object>() {{
        put("name", "generator-test");
        put("version", "latest");
      }});
    }};

    generator.add(map);
    final Map<String, String> result = session.close();
    KubernetesList list=session.getGeneratedResources().get("knative");
    assertThat(list).isNotNull();
    assertThat(list.getItems())
      .filteredOn(i -> "Service".equals(i.getKind()))
      .isNotEmpty();

    assertThat(tempDir.resolve("knative.json")).exists();
    assertThat(tempDir.resolve("knative.yml")).exists();

    assertThat(result).hasSize(4);
  }
}
