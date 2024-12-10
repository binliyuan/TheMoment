package com.volcengine.effectone.auto.templates.download

import java.io.IOException

/**
 *Author: gaojin
 *Time: 2022/10/31 14:40
 */

class FileNotAvailableException(msg:String) : IOException(msg)
class UnzipErrorException(msg:String) : IOException(msg)