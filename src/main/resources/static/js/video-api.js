// Video API AJAX Handler
const VideoAPI = {
    baseUrl: '/api/videos',
    
    // Lấy danh sách video với phân trang và tìm kiếm
    getVideos: function(keyword = '', page = 0, size = 10, sortBy = 'videoId', sortDir = 'asc') {
        return $.ajax({
            url: this.baseUrl,
            method: 'GET',
            data: {
                keyword: keyword,
                page: page,
                size: size,
                sortBy: sortBy,
                sortDir: sortDir
            },
            dataType: 'json'
        });
    },
    
    // Lấy video theo ID
    getVideoById: function(id) {
        return $.ajax({
            url: `${this.baseUrl}/${id}`,
            method: 'GET',
            dataType: 'json'
        });
    },
    
    // Tạo video mới
    createVideo: function(formData) {
        return $.ajax({
            url: this.baseUrl,
            method: 'POST',
            data: formData,
            processData: false,
            contentType: false,
            dataType: 'json'
        });
    },
    
    // Cập nhật video
    updateVideo: function(id, formData) {
        return $.ajax({
            url: `${this.baseUrl}/${id}`,
            method: 'PUT',
            data: formData,
            processData: false,
            contentType: false,
            dataType: 'json'
        });
    },
    
    // Xóa video
    deleteVideo: function(id) {
        return $.ajax({
            url: `${this.baseUrl}/${id}`,
            method: 'DELETE',
            dataType: 'json'
        });
    }
};

// Video UI Handler
const VideoUI = {
    currentPage: 0,
    pageSize: 10,
    currentKeyword: '',
    
    // Khởi tạo
    init: function() {
        this.loadVideos();
        this.bindEvents();
    },
    
    // Bind events
    bindEvents: function() {
        const self = this;
        
        // Tìm kiếm
        $('#searchForm').on('submit', function(e) {
            e.preventDefault();
            self.currentKeyword = $('#keywordInput').val();
            self.currentPage = 0;
            self.loadVideos();
        });
        
        // Thay đổi kích thước trang
        $('#pageSizeSelect').on('change', function() {
            self.pageSize = $(this).val();
            self.currentPage = 0;
            self.loadVideos();
        });
        
        // Form submit (create/update)
        $('#videoForm').on('submit', function(e) {
            e.preventDefault();
            self.saveVideo();
        });
        
        // Xóa video
        $(document).on('click', '.btn-delete-video', function() {
            const videoId = $(this).data('id');
            if (confirm('Bạn có chắc muốn xóa video này?')) {
                self.deleteVideo(videoId);
            }
        });
        
        // Sửa video
        $(document).on('click', '.btn-edit-video', function() {
            const videoId = $(this).data('id');
            self.editVideo(videoId);
        });
        
        // Reset form
        $('#btnReset').on('click', function() {
            self.resetForm();
        });
    },
    
    // Load danh sách video
    loadVideos: function() {
        const self = this;
        this.showLoading();
        
        VideoAPI.getVideos(this.currentKeyword, this.currentPage, this.pageSize)
            .done(function(response) {
                if (response.success) {
                    self.renderVideos(response.data);
                    self.renderPagination(response.data);
                } else {
                    self.showError(response.message);
                }
            })
            .fail(function(xhr) {
                const errorMsg = xhr.responseJSON?.message || 'Lỗi khi tải dữ liệu';
                self.showError(errorMsg);
            })
            .always(function() {
                self.hideLoading();
            });
    },
    
    // Render danh sách video
    renderVideos: function(pageData) {
        const tbody = $('#videoTableBody');
        tbody.empty();
        
        if (!pageData.content || pageData.content.length === 0) {
            tbody.append('<tr><td colspan="7" class="text-center text-muted">Không có dữ liệu</td></tr>');
            return;
        }
        
        pageData.content.forEach(function(video) {
            const row = `
                <tr>
                    <td>${video.videoId}</td>
                    <td>${video.title || ''}</td>
                    <td>
                        ${video.poster ? 
                            `<img src="/upload/video/${video.poster}" alt="poster" 
                                  style="height:40px; border-radius:5px;"
                                  onerror="this.src='https://via.placeholder.com/60x40'">` :
                            `<img src="https://via.placeholder.com/60x40" alt="poster" 
                                  style="height:40px; border-radius:5px;">`
                        }
                    </td>
                    <td>
                        <span class="badge bg-info">
                            <i class="bi bi-eye"></i> ${video.views || 0}
                        </span>
                    </td>
                    <td>${video.categoryName || 'N/A'}</td>
                    <td>
                        ${video.active ? 
                            '<span class="badge bg-success">Active</span>' : 
                            '<span class="badge bg-danger">Inactive</span>'
                        }
                    </td>
                    <td>
                        <button class="btn btn-sm btn-warning btn-edit-video" data-id="${video.videoId}">
                            <i class="bi bi-pencil"></i>
                        </button>
                        <button class="btn btn-sm btn-danger btn-delete-video" data-id="${video.videoId}">
                            <i class="bi bi-trash"></i>
                        </button>
                    </td>
                </tr>
            `;
            tbody.append(row);
        });
    },
    
    // Render phân trang
    renderPagination: function(pageData) {
        const pagination = $('#pagination');
        pagination.empty();
        
        if (pageData.totalPages <= 1) {
            return;
        }
        
        let paginationHTML = '<ul class="pagination mb-0">';
        
        // Previous
        const prevDisabled = pageData.currentPage === 0 ? 'disabled' : '';
        paginationHTML += `
            <li class="page-item ${prevDisabled}">
                <a class="page-link" href="#" data-page="${pageData.currentPage - 1}">
                    <i class="bi bi-chevron-left"></i>
                </a>
            </li>
        `;
        
        // Page numbers
        for (let i = 0; i < pageData.totalPages; i++) {
            const active = i === pageData.currentPage ? 'active' : '';
            paginationHTML += `
                <li class="page-item ${active}">
                    <a class="page-link" href="#" data-page="${i}">${i + 1}</a>
                </li>
            `;
        }
        
        // Next
        const nextDisabled = pageData.currentPage >= pageData.totalPages - 1 ? 'disabled' : '';
        paginationHTML += `
            <li class="page-item ${nextDisabled}">
                <a class="page-link" href="#" data-page="${pageData.currentPage + 1}">
                    <i class="bi bi-chevron-right"></i>
                </a>
            </li>
        `;
        
        paginationHTML += '</ul>';
        pagination.html(paginationHTML);
        
        // Bind pagination click
        pagination.find('.page-link').on('click', function(e) {
            e.preventDefault();
            const page = $(this).data('page');
            if (page !== undefined && !$(this).parent().hasClass('disabled')) {
                VideoUI.currentPage = page;
                VideoUI.loadVideos();
            }
        });
        
        // Update info
        $('#paginationInfo').html(`
            Hiển thị <strong>${pageData.content.length}</strong> / 
            <strong>${pageData.totalElements}</strong> video
        `);
    },
    
    // Lấy video để sửa
    editVideo: function(id) {
        const self = this;
        this.showLoading();
        
        VideoAPI.getVideoById(id)
            .done(function(response) {
                if (response.success) {
                    self.fillForm(response.data);
                    $('#videoForm').data('edit-id', id);
                    $('#formTitle').text('Cập nhật video');
                    $('html, body').animate({ scrollTop: $('#videoForm').offset().top }, 500);
                } else {
                    self.showError(response.message);
                }
            })
            .fail(function(xhr) {
                const errorMsg = xhr.responseJSON?.message || 'Lỗi khi tải dữ liệu';
                self.showError(errorMsg);
            })
            .always(function() {
                self.hideLoading();
            });
    },
    
    // Điền form
    fillForm: function(video) {
        $('#videoId').val(video.videoId).prop('readonly', true);
        $('#title').val(video.title);
        $('#description').val(video.description);
        $('#views').val(video.views);
        $('#categoryId').val(video.categoryId);
        $('#active').prop('checked', video.active);
        
        if (video.poster) {
            $('#currentPoster').html(`
                <img src="/upload/video/${video.poster}" alt="poster" 
                     style="height:100px; border-radius:8px;"
                     onerror="this.style.display='none'">
            `).show();
        }
    },
    
    // Lưu video (create/update)
    saveVideo: function() {
        const self = this;
        const form = $('#videoForm')[0];
        const formData = new FormData(form);
        const editId = $('#videoForm').data('edit-id');
        
        // Xử lý checkbox active (nếu không check thì không gửi, cần set false)
        if (!$('#active').is(':checked')) {
            formData.set('active', 'false');
        } else {
            formData.set('active', 'true');
        }
        
        this.showLoading();
        
        const apiCall = editId 
            ? VideoAPI.updateVideo(editId, formData)
            : VideoAPI.createVideo(formData);
        
        apiCall
            .done(function(response) {
                if (response.success) {
                    self.showSuccess(response.message || 'Lưu thành công!');
                    self.resetForm();
                    $('#videoModal').modal('hide');
                    self.loadVideos();
                } else {
                    self.showError(response.message);
                }
            })
            .fail(function(xhr) {
                let errorMsg = 'Lỗi khi lưu video';
                if (xhr.responseJSON) {
                    errorMsg = xhr.responseJSON.message || errorMsg;
                }
                self.showError(errorMsg);
            })
            .always(function() {
                self.hideLoading();
            });
    },
    
    // Xóa video
    deleteVideo: function(id) {
        const self = this;
        this.showLoading();
        
        VideoAPI.deleteVideo(id)
            .done(function(response) {
                if (response.success) {
                    self.showSuccess(response.message || 'Xóa thành công!');
                    self.loadVideos();
                } else {
                    self.showError(response.message);
                }
            })
            .fail(function(xhr) {
                const errorMsg = xhr.responseJSON?.message || 'Lỗi khi xóa video';
                self.showError(errorMsg);
            })
            .always(function() {
                self.hideLoading();
            });
    },
    
    // Reset form
    resetForm: function() {
        $('#videoForm')[0].reset();
        $('#videoForm').removeData('edit-id');
        $('#videoId').prop('readonly', false);
        $('#currentPoster').hide();
        $('#formTitle').text('Thêm mới video');
    },
    
    // Hiển thị loading
    showLoading: function() {
        $('#loadingSpinner').show();
    },
    
    hideLoading: function() {
        $('#loadingSpinner').hide();
    },
    
    // Hiển thị thông báo
    showSuccess: function(message) {
        this.showAlert(message, 'success');
    },
    
    showError: function(message) {
        this.showAlert(message, 'danger');
    },
    
    showAlert: function(message, type) {
        const alertHTML = `
            <div class="alert alert-${type} alert-dismissible fade show" role="alert">
                ${message}
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
        `;
        $('#alertContainer').html(alertHTML);
        
        // Auto dismiss after 5 seconds
        setTimeout(function() {
            $('#alertContainer .alert').alert('close');
        }, 5000);
    }
};

// Initialize when document ready
$(document).ready(function() {
    VideoUI.init();
});

