// SPDX-FileCopyrightText: Copyright 2026 Eden Emulator Project
// SPDX-License-Identifier: GPL-3.0-or-later

// SPDX-FileCopyrightText: 2013 Dolphin Emulator Project
// SPDX-FileCopyrightText: 2014 Citra Emulator Project
// SPDX-License-Identifier: GPL-2.0-or-later

#include <algorithm>
#include <cctype>
#include <locale>
#include <sstream>
#include <string_view>

#include "common/string_util.h"

#ifdef _WIN32
#include <windows.h>
#endif

#ifdef __ANDROID__
#include <common/fs/fs_android.h>
#endif

namespace Common {

/// Make a string lowercase
std::string ToLower(const std::string_view sv) {
    std::string str{sv};
    std::transform(str.begin(), str.end(), str.begin(),
                   [](auto const c) { return char(std::tolower(c)); });
    return str;
}

/// Make a string uppercase
std::string ToUpper(const std::string_view sv) {
    std::string str{sv};
    std::transform(str.begin(), str.end(), str.begin(),
                   [](auto const c) { return char(std::toupper(c)); });
    return str;
}

bool SplitPath(const std::string& full_path, std::string* _pPath, std::string* _pFilename,
               std::string* _pExtension) {
    if (full_path.empty())
        return false;

#ifdef __ANDROID__
    if (full_path[0] != '/') {
        *_pPath = Common::FS::Android::GetParentDirectory(full_path);
        *_pFilename = Common::FS::Android::GetFilename(full_path);
        return true;
    }
#endif

    std::size_t dir_end = full_path.find_last_of("/"
// windows needs the : included for something like just "C:" to be considered a directory
#ifdef _WIN32
                                                 "\\:"
#endif
    );
    if (std::string::npos == dir_end)
        dir_end = 0;
    else
        dir_end += 1;

    std::size_t fname_end = full_path.rfind('.');
    if (fname_end < dir_end || std::string::npos == fname_end)
        fname_end = full_path.size();

    if (_pPath)
        *_pPath = full_path.substr(0, dir_end);

    if (_pFilename)
        *_pFilename = full_path.substr(dir_end, fname_end - dir_end);

    if (_pExtension)
        *_pExtension = full_path.substr(fname_end);

    return true;
}

void SplitString(const std::string& str, const char delim, std::vector<std::string>& output) {
    std::istringstream iss(str);
    output.resize(1);

    while (std::getline(iss, *output.rbegin(), delim)) {
        output.emplace_back();
    }

    output.pop_back();
}

std::string TabsToSpaces(int tab_size, std::string in) {
    std::size_t i = 0;

    while ((i = in.find('\t')) != std::string::npos) {
        in.replace(i, 1, tab_size, ' ');
    }

    return in;
}

std::string ReplaceAll(std::string result, const std::string& src, const std::string& dest) {
    std::size_t pos = 0;

    if (src == dest)
        return result;

    while ((pos = result.find(src, pos)) != std::string::npos) {
        result.replace(pos, src.size(), dest);
        pos += dest.length();
    }

    return result;
}

namespace {
// Hand-written UTF conversions (std::wstring_convert/codecvt are deprecated). Malformed input
// becomes U+FFFD instead of throwing.
constexpr char32_t ReplacementChar = 0xFFFD;

void AppendUTF8(std::string& out, char32_t cp) {
    if (cp > 0x10FFFF || (cp >= 0xD800 && cp <= 0xDFFF)) {
        cp = ReplacementChar;
    }
    if (cp < 0x80) {
        out.push_back(static_cast<char>(cp));
    } else if (cp < 0x800) {
        out.push_back(static_cast<char>(0xC0 | (cp >> 6)));
        out.push_back(static_cast<char>(0x80 | (cp & 0x3F)));
    } else if (cp < 0x10000) {
        out.push_back(static_cast<char>(0xE0 | (cp >> 12)));
        out.push_back(static_cast<char>(0x80 | ((cp >> 6) & 0x3F)));
        out.push_back(static_cast<char>(0x80 | (cp & 0x3F)));
    } else {
        out.push_back(static_cast<char>(0xF0 | (cp >> 18)));
        out.push_back(static_cast<char>(0x80 | ((cp >> 12) & 0x3F)));
        out.push_back(static_cast<char>(0x80 | ((cp >> 6) & 0x3F)));
        out.push_back(static_cast<char>(0x80 | (cp & 0x3F)));
    }
}

/// Decodes one code point starting at input[pos] and advances pos past it.
char32_t DecodeUTF8(std::string_view input, size_t& pos) {
    const auto byte = [&](size_t i) { return static_cast<u8>(input[i]); };
    const u8 lead = byte(pos);
    size_t length;
    char32_t cp;
    char32_t min;
    if (lead < 0x80) {
        ++pos;
        return lead;
    } else if ((lead & 0xE0) == 0xC0) {
        length = 2, cp = lead & 0x1F, min = 0x80;
    } else if ((lead & 0xF0) == 0xE0) {
        length = 3, cp = lead & 0x0F, min = 0x800;
    } else if ((lead & 0xF8) == 0xF0) {
        length = 4, cp = lead & 0x07, min = 0x10000;
    } else {
        ++pos;
        return ReplacementChar;
    }
    if (pos + length > input.size()) {
        ++pos;
        return ReplacementChar;
    }
    for (size_t i = 1; i < length; ++i) {
        const u8 cont = byte(pos + i);
        if ((cont & 0xC0) != 0x80) {
            ++pos;
            return ReplacementChar;
        }
        cp = (cp << 6) | (cont & 0x3F);
    }
    pos += length;
    if (cp < min || cp > 0x10FFFF || (cp >= 0xD800 && cp <= 0xDFFF)) {
        return ReplacementChar;
    }
    return cp;
}
} // Anonymous namespace

std::string UTF16ToUTF8(std::u16string_view input) {
    std::string out;
    out.reserve(input.size());
    for (size_t i = 0; i < input.size(); ++i) {
        char32_t cp = input[i];
        if (cp >= 0xD800 && cp <= 0xDBFF && i + 1 < input.size() && input[i + 1] >= 0xDC00 &&
            input[i + 1] <= 0xDFFF) {
            cp = 0x10000 + ((cp - 0xD800) << 10) + (input[i + 1] - 0xDC00);
            ++i;
        }
        AppendUTF8(out, cp);
    }
    return out;
}

std::u16string UTF8ToUTF16(std::string_view input) {
    std::u16string out;
    out.reserve(input.size());
    for (size_t pos = 0; pos < input.size();) {
        const char32_t cp = DecodeUTF8(input, pos);
        if (cp >= 0x10000) {
            const char32_t v = cp - 0x10000;
            out.push_back(static_cast<char16_t>(0xD800 + (v >> 10)));
            out.push_back(static_cast<char16_t>(0xDC00 + (v & 0x3FF)));
        } else {
            out.push_back(static_cast<char16_t>(cp));
        }
    }
    return out;
}

std::u32string UTF8ToUTF32(std::string_view input) {
    std::u32string out;
    out.reserve(input.size());
    for (size_t pos = 0; pos < input.size();) {
        out.push_back(DecodeUTF8(input, pos));
    }
    return out;
}

#ifdef _WIN32
static std::wstring CPToUTF16(u32 code_page, std::string_view input) {
    const auto size =
        MultiByteToWideChar(code_page, 0, input.data(), static_cast<int>(input.size()), nullptr, 0);

    if (size == 0) {
        return {};
    }

    std::wstring output(size, L'\0');

    if (size != MultiByteToWideChar(code_page, 0, input.data(), static_cast<int>(input.size()),
                                    &output[0], static_cast<int>(output.size()))) {
        output.clear();
    }

    return output;
}

std::string UTF16ToUTF8(std::wstring_view input) {
    const auto size = WideCharToMultiByte(CP_UTF8, 0, input.data(), static_cast<int>(input.size()),
                                          nullptr, 0, nullptr, nullptr);
    if (size == 0) {
        return {};
    }

    std::string output(size, '\0');

    if (size != WideCharToMultiByte(CP_UTF8, 0, input.data(), static_cast<int>(input.size()),
                                    &output[0], static_cast<int>(output.size()), nullptr,
                                    nullptr)) {
        output.clear();
    }

    return output;
}

std::wstring UTF8ToUTF16W(std::string_view input) {
    return CPToUTF16(CP_UTF8, input);
}

#endif

std::u16string U16StringFromBuffer(const u16* input, std::size_t length) {
    return std::u16string(reinterpret_cast<const char16_t*>(input), length);
}

std::string StringFromFixedZeroTerminatedBuffer(std::string_view buffer, std::size_t max_len) {
    std::size_t len = 0;
    while (len < buffer.length() && len < max_len && buffer[len] != '\0') {
        ++len;
    }
    return std::string(buffer.begin(), buffer.begin() + len);
}

std::u16string UTF16StringFromFixedZeroTerminatedBuffer(std::u16string_view buffer,
                                                        std::size_t max_len) {
    std::size_t len = 0;
    while (len < buffer.length() && len < max_len && buffer[len] != '\0') {
        ++len;
    }
    return std::u16string(buffer.begin(), buffer.begin() + len);
}

} // namespace Common
