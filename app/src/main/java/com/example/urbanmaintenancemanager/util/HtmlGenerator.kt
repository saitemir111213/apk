package com.example.urbanmaintenancemanager.util

import android.content.Context
import com.example.urbanmaintenancemanager.data.local.model.ReportWithDetails
import saman.zamani.persiandate.PersianDate
import saman.zamani.persiandate.PersianDateFormat
import java.io.IOException

class HtmlGenerator(private val context: Context) {

    private val pfd = PersianDateFormat("Y/m/d")

    fun generate(
        reports: List<ReportWithDetails>,
        dateRange: String
    ): String {
        return try {
            val template = context.assets.open("report_template.html").bufferedReader().use { it.readText() }
            val reportItemsHtml = reports.joinToString(separator = "") { report ->
                buildReportItem(report)
            }
            template
                .replace("{{date_range}}", dateRange)
                .replace("{{report_items}}", reportItemsHtml)
        } catch (e: IOException) {
            e.printStackTrace()
            "<html><body><h1>خطا در تولید گزارش</h1><p>${e.message}</p></body></html>"
        }
    }

    private fun buildReportItem(reportWithDetails: ReportWithDetails): String {
        val persianDate = pfd.format(PersianDate(reportWithDetails.report.date))
        val drawingsHtml = reportWithDetails.drawings.joinToString("") { drawing ->
            buildDrawingSection(drawing)
        }

        return """
            <div class="report-item">
                <h2>گزارش تاریخ: $persianDate</h2>
                <p><strong>توضیحات کلی:</strong> ${reportWithDetails.report.description}</p>
                $drawingsHtml
            </div>
        """
    }

    private fun buildDrawingSection(drawingWithWorkers: com.example.urbanmaintenancemanager.data.local.model.DrawingWithWorkers): String {
        val workersTable = buildWorkersTable(drawingWithWorkers.workers)
        return """
            <div class="drawing-section">
                <h3>عملیات: ${drawingWithWorkers.drawing.taskType}</h3>
                <p><strong>آدرس:</strong> ${drawingWithWorkers.drawing.address ?: "ثبت نشده"}</p>
                <p><strong>توضیحات:</strong> ${drawingWithWorkers.drawing.description}</p>
                <p><strong>ساعات کار:</strong> ${drawingWithWorkers.drawing.hours}</p>
                <h4>کارکنان</h4>
                $workersTable
                <div class="map-placeholder">
                    مکان نقشه برای عملیات فوق
                </div>
            </div>
        """
    }

    private fun buildWorkersTable(workers: List<com.example.urbanmaintenancemanager.data.local.model.Worker>): String {
        val rows = workers.joinToString("") { worker ->
            "<tr><td>${worker.name}</td><td>${worker.phoneNumber}</td></tr>"
        }
        return """
            <table>
                <thead>
                    <tr>
                        <th>نام کارگر</th>
                        <th>شماره تماس</th>
                    </tr>
                </thead>
                <tbody>
                    $rows
                </tbody>
            </table>
        """
    }
} 