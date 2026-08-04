import React, { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import { Upload, FileText, CheckCircle2, X, CloudUpload, Star, Loader2, Trash2 } from 'lucide-react';
import {
  Card, CardContent, CardHeader, CardTitle, CardDescription,
} from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { Badge } from '../components/ui/Badge';
import { PageHeader } from '../components/ui/PageHeader';
import apiClient, { API_ENDPOINTS } from '../lib/apiClient';

interface ResumeItem {
  id: string;
  title?: string;
  originalFilename?: string;
  isPrimary?: boolean;
  status?: string;
  summary?: string;
  skills?: { name: string }[];
  workExperiences?: { jobTitle?: string; companyName?: string; description?: string }[];
}

function unwrapList(payload: any): ResumeItem[] {
  const data = payload?.data ?? payload;
  if (Array.isArray(data)) return data;
  if (Array.isArray(data?.content)) return data.content;
  return [];
}

export function ResumeUpload() {
  const navigate = useNavigate();
  const [isDragging, setIsDragging] = useState(false);
  const [isUploading, setIsUploading] = useState(false);
  const [uploaded, setUploaded] = useState(false);
  const [loading, setLoading] = useState(true);
  const [resumes, setResumes] = useState<ResumeItem[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [deletingId, setDeletingId] = useState<string | null>(null);
  const fileInputRef = React.useRef<HTMLInputElement>(null);

  const loadResumes = async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await apiClient.get(API_ENDPOINTS.RESUME.LIST);
      const list = unwrapList(res.data ?? res);
      setResumes(list);
      setUploaded(list.length > 0);
    } catch (err: any) {
      console.warn('Failed to list resumes:', err);
      setError(err?.message || 'Unable to load resumes.');
      setUploaded(false);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadResumes();
  }, []);

  const handleDragOver = (e: React.DragEvent) => {
    e.preventDefault();
    setIsDragging(true);
  };
  const handleDragLeave = () => setIsDragging(false);

  const uploadFile = async (file: File) => {
    if (!file) return;
    setIsUploading(true);
    setError(null);
    try {
      const form = new FormData();
      form.append('file', file);
      form.append('title', file.name.replace(/\.[^.]+$/, '') || 'My Resume');
      form.append('setPrimary', String(resumes.length === 0));
      await apiClient.post(API_ENDPOINTS.RESUME.UPLOAD, form);
      await loadResumes();
      setUploaded(true);
    } catch (err: any) {
      console.warn('Resume upload failed:', err);
      setError(err?.message || 'Upload failed. Please try again.');
    } finally {
      setIsUploading(false);
    }
  };

  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault();
    setIsDragging(false);
    const file = e.dataTransfer.files?.[0];
    if (file) void uploadFile(file);
  };

  const handleBrowse = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) void uploadFile(file);
    e.target.value = '';
  };

  const setPrimary = async (id: string) => {
    try {
      await apiClient.put(API_ENDPOINTS.RESUME.PRIMARY(id));
      await loadResumes();
    } catch (err: any) {
      console.warn('Set primary failed:', err);
      setError(err?.message || 'Could not set primary resume.');
    }
  };

  const deleteResume = async (id: string) => {
    if (!window.confirm('Are you sure you want to delete this resume?')) return;
    setDeletingId(id);
    try {
      await apiClient.delete(API_ENDPOINTS.RESUME.DELETE(id));
      await loadResumes();
    } catch (err: any) {
      console.warn('Delete resume failed:', err);
      setError(err?.message || 'Could not delete resume.');
    } finally {
      setDeletingId(null);
    }
  };

  const primary = resumes.find((r) => r.isPrimary) || resumes[0];

  return (
    <div className="mx-auto max-w-4xl space-y-8">
      <PageHeader
        title="Resume Context"
        subtitle="Upload your latest resume. Our AI will analyze it to personalize your interview questions."
      />

      {error && (
        <div className="rounded-xl border border-rose-500/30 bg-rose-500/10 px-4 py-3 text-sm text-rose-300">
          {error}
        </div>
      )}

      {loading ? (
        <div className="flex items-center justify-center py-20 text-ink-400">
          <Loader2 className="h-8 w-8 animate-spin text-brand-500" />
        </div>
      ) : (
        <AnimatePresence mode="wait">
          {!uploaded ? (
            <motion.div
              key="upload"
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, scale: 0.97 }}
              transition={{ duration: 0.35 }}
            >
              <Card>
                <CardContent className="p-8">
                  <motion.div
                    onDragOver={handleDragOver}
                    onDragLeave={handleDragLeave}
                    onDrop={handleDrop}
                    animate={isDragging ? { scale: 1.02 } : { scale: 1 }}
                    transition={{ duration: 0.2 }}
                    className={`flex flex-col items-center justify-center rounded-2xl border-2 border-dashed p-14 text-center transition-all duration-300 ${
                      isDragging
                        ? 'border-brand-500 bg-brand-500/5 shadow-glow-sm'
                        : 'border-ink-200 dark:border-white/10 hover:border-brand-400/50 dark:hover:border-brand-500/30 hover:bg-ink-50 dark:hover:bg-white/[0.02]'
                    }`}
                  >
                    <motion.div
                      animate={isDragging ? { scale: 1.15, y: -4 } : { scale: 1, y: 0 }}
                      transition={{ duration: 0.25 }}
                      className="mb-5 flex h-16 w-16 items-center justify-center rounded-2xl bg-brand-500/10 text-brand-500"
                    >
                      <CloudUpload size={32} />
                    </motion.div>
                    <h3 className="mb-2 text-lg font-semibold text-ink-900 dark:text-white">
                      {isDragging ? 'Drop it here!' : 'Drag & drop your resume'}
                    </h3>
                    <p className="mb-6 text-sm text-ink-400 dark:text-ink-500">
                      Supports PDF, DOC, DOCX — Max 5MB
                    </p>
                    <input
                      ref={fileInputRef}
                      type="file"
                      accept=".pdf,.doc,.docx,application/pdf"
                      className="hidden"
                      onChange={handleBrowse}
                    />
                    <Button
                      onClick={() => fileInputRef.current?.click()}
                      isLoading={isUploading}
                      variant="gradient"
                    >
                      <Upload size={15} /> Browse Files
                    </Button>
                  </motion.div>
                </CardContent>
              </Card>
            </motion.div>
          ) : (
            <motion.div
              key="uploaded"
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, y: -10 }}
              transition={{ duration: 0.4 }}
              className="space-y-5"
            >
              <div className="flex items-center justify-between gap-4">
                <p className="text-sm text-ink-500">
                  {resumes.length} resume{resumes.length === 1 ? '' : 's'} on file
                </p>
                <div className="flex gap-2">
                  <input
                    ref={fileInputRef}
                    type="file"
                    accept=".pdf,.doc,.docx,application/pdf"
                    className="hidden"
                    onChange={handleBrowse}
                  />
                  <Button
                    variant="outline"
                    size="sm"
                    isLoading={isUploading}
                    onClick={() => fileInputRef.current?.click()}
                  >
                    <Upload size={14} /> Upload another
                  </Button>
                </div>
              </div>

              {resumes.map((resume) => (
                <Card key={resume.id} hoverable>
                  <CardHeader className="flex flex-row items-center justify-between gap-4">
                    <div className="flex items-center gap-4 min-w-0">
                      <div className="flex h-12 w-12 shrink-0 items-center justify-center rounded-xl bg-brand-500/10 text-brand-500">
                        <FileText size={22} />
                      </div>
                      <div className="min-w-0">
                        <CardTitle className="truncate">
                          {resume.title || resume.originalFilename || 'Resume'}
                        </CardTitle>
                        <CardDescription>
                          {resume.status || 'READY'}
                          {resume.isPrimary ? ' • Primary' : ''}
                        </CardDescription>
                      </div>
                    </div>
                    <div className="flex items-center gap-2 shrink-0">
                      {resume.isPrimary ? (
                        <Badge variant="success" className="gap-1">
                          <Star size={12} /> Primary
                        </Badge>
                      ) : (
                        <Button variant="ghost" size="sm" onClick={() => setPrimary(resume.id)}>
                          Set primary
                        </Button>
                      )}
                      <Button
                        variant="ghost"
                        size="sm"
                        className="text-red-500 hover:text-red-600 hover:bg-red-500/10 transition-colors"
                        onClick={() => deleteResume(resume.id)}
                        isLoading={deletingId === resume.id}
                        title="Delete resume"
                      >
                        <Trash2 size={16} />
                      </Button>
                    </div>
                  </CardHeader>
                  {(resume.summary || (resume.skills && resume.skills.length > 0)) && (
                    <CardContent className="space-y-3">
                      {resume.summary && (
                        <p className="text-sm text-ink-500 dark:text-ink-400 leading-relaxed">
                          {resume.summary}
                        </p>
                      )}
                      {resume.skills && resume.skills.length > 0 && (
                        <div className="flex flex-wrap gap-2">
                          {resume.skills.slice(0, 12).map((skill, i) => (
                            <Badge key={i} variant="secondary">
                              {skill.name}
                            </Badge>
                          ))}
                        </div>
                      )}
                    </CardContent>
                  )}
                </Card>
              ))}

              <Button
                className="w-full gap-2"
                variant="gradient"
                onClick={() => navigate('/interview/company')}
                disabled={!primary}
              >
                Continue to Interview Setup <CheckCircle2 size={16} />
              </Button>
            </motion.div>
          )}
        </AnimatePresence>
      )}
    </div>
  );
}
