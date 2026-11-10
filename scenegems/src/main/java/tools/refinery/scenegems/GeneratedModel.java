/*
 * SPDX-FileCopyrightText: 2026 The Refinery Authors <https://refinery.tools/>
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package tools.refinery.scenegems;

import org.jetbrains.annotations.Nullable;

/**
 * One generated model.
 *
 * @param index  The order in which the model was found, starting from 0.
 * @param file   The path of the model's {@code .problem} file relative to the output folder, with {@code /}
 *               separators.
 * @param source The Refinery source of the model, or {@code null} if the request asked for files only.
 */
public record GeneratedModel(int index, String file, @Nullable String source) {
}
