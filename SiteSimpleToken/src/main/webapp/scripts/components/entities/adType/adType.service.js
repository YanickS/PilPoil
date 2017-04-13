'use strict';

angular.module('pilpoilwebApp')
    .factory('AdType', function ($resource, DateUtils) {
        return $resource('api/adTypes/:id', {}, {
            'query': { method: 'GET', isArray: true},
            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    data = angular.fromJson(data);
                    return data;
                }
            },
            'update': { method:'PUT' }
        });
    });