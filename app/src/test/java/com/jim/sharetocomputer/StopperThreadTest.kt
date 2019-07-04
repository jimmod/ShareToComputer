/*
    This file is part of Share To Computer  Copyright (C) 2019  Jimmy <https://github.com/jimmod/ShareToComputer>.

    Share To Computer is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Share To Computer is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Share To Computer.  If not, see <https://www.gnu.org/licenses/>.
*/
import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.jim.sharetocomputer.*
import com.jim.sharetocomputer.webserver.WebServer
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = RobolectricApplication::class)
class StopperThreadTest {

    private val application: Application by lazy { ApplicationProvider.getApplicationContext<Application>() }

    @Test
    fun service_stopself_automatically() {
        val intent = WebServerService.createIntent(application, ShareRequest.ShareRequestText("Hello"))
        val serviceController = Robolectric.buildService(WebServerService::class.java, intent).create()
        val service = Shadows.shadowOf(serviceController.get())
        val webServer = WebServerMock()

        StopperThread(serviceController.get(), webServer, 1).start()

        assertTimeout(2000) {
            Assert.assertEquals(true, service.isStoppedBySelf)
        }
    }

}

private class WebServerMock : WebServer(8080) {

    override fun start() {
    }

}
